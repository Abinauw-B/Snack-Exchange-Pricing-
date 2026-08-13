package com.retailpos.pos;

import com.retailpos.domain.*;
import com.retailpos.inventory.JuiceBatchService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class POSService {

    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final JuiceBatchService juiceBatchService;

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

        for (CartItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + itemReq.getProductId()));

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
