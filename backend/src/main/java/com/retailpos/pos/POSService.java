package com.retailpos.pos;

import com.retailpos.domain.*;
import com.retailpos.inventory.JuiceBatchService;
import com.retailpos.pricing.MarketCrashService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class POSService {

    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final JuiceBatchService juiceBatchService;
    private final MarketCrashService marketCrashService;

    @Data
    public static class CartItemRequest {
        private Long productId;
        private Integer quantity;
        private Integer cupSizeMl; // Defaults to 250ml
    }

    @Data
    public static class CheckoutRequest {
        private List<CartItemRequest> items;
        private String paymentMethod; // CASH, UPI, CARD
    }

    @Data
    @Builder
    public static class CheckoutResponse {
        private String orderNumber;
        private BigDecimal totalAmount;
        private String paymentMethod;
        private String paymentStatus;
        private LocalDateTime timestamp;
        private List<OrderItemResponse> items;
    }

    @Data
    @Builder
    public static class OrderItemResponse {
        private String productName;
        private Integer quantity;
        private Integer cupSizeMl;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private Integer volumeDeductedMl;
    }

    @Transactional
    public CheckoutResponse processCheckout(CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty for checkout");
        }

        String orderNum = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String paymentMethod = (request.getPaymentMethod() != null) ? request.getPaymentMethod().toUpperCase() : "CASH";

        BigDecimal orderTotal = BigDecimal.ZERO;
        List<SalesOrderItem> orderItems = new ArrayList<>();
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        SalesOrder salesOrder = SalesOrder.builder()
                .orderNumber(orderNum)
                .totalAmount(BigDecimal.ZERO)
                .paymentMethod(paymentMethod)
                .paymentStatus("COMPLETED")
                .createdAt(LocalDateTime.now())
                .build();

        Set<Long> purchasedProductIds = new HashSet<>();

        for (CartItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + itemReq.getProductId()));

            purchasedProductIds.add(product.getId());

            int cupSize = (itemReq.getCupSizeMl() != null && itemReq.getCupSizeMl() > 0) ? itemReq.getCupSizeMl() : product.getDefaultCupSizeMl();
            int qty = (itemReq.getQuantity() != null && itemReq.getQuantity() > 0) ? itemReq.getQuantity() : 1;

            int totalVolumeMl = cupSize * qty;

            // Atomically deduct volume from active batch using pessimistic lock
            JuiceBatch updatedBatch = juiceBatchService.deductBatchVolume(product.getId(), totalVolumeMl);

            BigDecimal unitPrice = product.getCurrentCupPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            orderTotal = orderTotal.add(itemTotal);

            SalesOrderItem orderItem = SalesOrderItem.builder()
                    .salesOrder(salesOrder)
                    .productId(product.getId())
                    .productName(product.getName())
                    .cupSizeMl(cupSize)
                    .unitPrice(unitPrice)
                    .quantity(qty)
                    .totalPrice(itemTotal)
                    .volumeDeductedMl(totalVolumeMl)
                    .build();

            orderItems.add(orderItem);

            itemResponses.add(OrderItemResponse.builder()
                    .productName(product.getName())
                    .quantity(qty)
                    .cupSizeMl(cupSize)
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotal)
                    .volumeDeductedMl(totalVolumeMl)
                    .build());
        }

        salesOrder.setTotalAmount(orderTotal);
        salesOrder.setItems(orderItems);
        salesOrderRepository.save(salesOrder);

        // Bar Stock Exchange dynamic price recalculation across all products
        if (marketCrashService == null || !marketCrashService.isCrashActive()) {
            List<Product> allProducts = productRepository.findAll();
            LocalDateTime now = LocalDateTime.now();

            int totalPurchasedQty = request.getItems().stream().mapToInt(CartItemRequest::getQuantity).sum();

            for (Product p : allProducts) {
                BigDecimal oldPrice = p.getCurrentCupPrice();
                BigDecimal newPrice = oldPrice;
                String explanation;

                if (purchasedProductIds.contains(p.getId())) {
                    // Purchased item -> Price surges proportionally (+1 or +2)
                    int surge = Math.min(2, Math.max(1, totalPurchasedQty));
                    newPrice = oldPrice.add(BigDecimal.valueOf(surge));
                    if (newPrice.compareTo(p.getMaxCupPrice()) > 0) {
                        newPrice = p.getMaxCupPrice();
                    }
                    explanation = String.format("📈 BAR STOCK SURGE: Buying volume surge (+%d cups). Price increased from ₹%s to ₹%s for %s.", totalPurchasedQty, oldPrice, newPrice, p.getFlavour());
                } else {
                    // Unpurchased items -> Dynamic market variation & capital shift discount (-1)
                    if (oldPrice.compareTo(p.getMinCupPrice()) > 0) {
                        newPrice = oldPrice.subtract(BigDecimal.ONE);
                        explanation = String.format("📉 BAR STOCK DIVERTS: Demand shifted away. Price discounted from ₹%s to ₹%s for %s.", oldPrice, newPrice, p.getFlavour());
                    } else {
                        explanation = String.format("Price for %s locked at absolute floor boundary ₹%s.", p.getFlavour(), p.getMinCupPrice());
                    }
                }

                if (newPrice.compareTo(oldPrice) != 0) {
                    p.setCurrentCupPrice(newPrice);
                    p.setLastPriceChangeTimestamp(now);
                    productRepository.save(p);

                    PriceHistory history = PriceHistory.builder()
                            .productId(p.getId())
                            .oldPrice(oldPrice)
                            .newPrice(newPrice)
                            .demandScore(purchasedProductIds.contains(p.getId()) ? 92.0 : 28.0)
                            .stockPressurePct(50.0)
                            .timeFactorMultiplier(1.0)
                            .explanation(explanation)
                            .createdAt(now)
                            .build();
                    priceHistoryRepository.save(history);
                }
            }
        }

        return CheckoutResponse.builder()
                .orderNumber(orderNum)
                .totalAmount(orderTotal)
                .paymentMethod(paymentMethod)
                .paymentStatus("COMPLETED")
                .timestamp(LocalDateTime.now())
                .items(itemResponses)
                .build();
    }
}

