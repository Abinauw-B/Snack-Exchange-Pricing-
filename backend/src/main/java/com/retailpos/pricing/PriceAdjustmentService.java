package com.retailpos.pricing;

import com.retailpos.domain.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceAdjustmentService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final DemandCalculationService demandCalculationService;
    private final StockPressureService stockPressureService;
    private final TimeFactorService timeFactorService;
    private final MarketCrashService marketCrashService;

    @Data
    @Builder
    public static class PriceEvaluationResult {
        private Long productId;
        private String flavour;
        private BigDecimal oldPrice;
        private BigDecimal newPrice;
        private boolean priceChanged;
        private double demandScore;
        private double stockPressurePct;
        private double timeFactorMultiplier;
        private String explanation;
        private String statusReason;
    }

    @Transactional
    public PriceEvaluationResult evaluateAndAdjustPrice(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        if (marketCrashService != null && marketCrashService.isCrashActive()) {
            return PriceEvaluationResult.builder()
                    .productId(productId)
                    .flavour(product.getFlavour())
                    .oldPrice(product.getMinCupPrice())
                    .newPrice(product.getMinCupPrice())
                    .priceChanged(false)
                    .demandScore(0.0)
                    .stockPressurePct(0.0)
                    .timeFactorMultiplier(1.0)
                    .explanation("🚨 Market Crash Routine Active! All products set to floor price.")
                    .statusReason("MARKET_CRASH_ACTIVE")
                    .build();
        }

        // Read Config weights
        double wVelocity = getConfigDouble("weight_velocity", 0.40);
        double wStock = getConfigDouble("weight_stock_pressure", 0.40);
        double wTime = getConfigDouble("weight_time_factor", 0.20);
        long cooldownMins = getConfigLong("cooldown_minutes", 10);

        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        // Check Cooldown
        if (product.getLastPriceChangeTimestamp() != null) {
            long minsSinceLastChange = Duration.between(product.getLastPriceChangeTimestamp(), now).toMinutes();
            if (minsSinceLastChange < cooldownMins) {
                double stockPct = stockPressureService.calculateStockPressurePercentage(productId);
                double timeMult = timeFactorService.getTimeFactorMultiplier(currentTime);
                double demandScore = demandCalculationService.calculateDemandScore(productId, wVelocity, wStock, wTime, currentTime);

                String explanation = String.format(
                    "Price maintained at ₹%s for %s. Price change on cooldown (%d/%d mins elapsed). Demand Score: %.1f.",
                    product.getCurrentCupPrice(), product.getFlavour(), minsSinceLastChange, cooldownMins, demandScore
                );

                return PriceEvaluationResult.builder()
                        .productId(productId)
                        .flavour(product.getFlavour())
                        .oldPrice(product.getCurrentCupPrice())
                        .newPrice(product.getCurrentCupPrice())
                        .priceChanged(false)
                        .demandScore(demandScore)
                        .stockPressurePct(stockPct)
                        .timeFactorMultiplier(timeMult)
                        .explanation(explanation)
                        .statusReason("COOLDOWN_ACTIVE")
                        .build();
            }
        }

        double stockPct = stockPressureService.calculateStockPressurePercentage(productId);
        double timeMult = timeFactorService.getTimeFactorMultiplier(currentTime);
        double demandScore = demandCalculationService.calculateDemandScore(productId, wVelocity, wStock, wTime, currentTime);

        BigDecimal currentPrice = product.getCurrentCupPrice();
        BigDecimal minPrice = product.getMinCupPrice();
        BigDecimal maxPrice = product.getMaxCupPrice();

        BigDecimal targetPrice = currentPrice;
        boolean changed = false;
        String actionText;

        if (demandScore >= 65.0) {
            if (currentPrice.compareTo(maxPrice) < 0) {
                targetPrice = currentPrice.add(BigDecimal.ONE); // + ₹1 step
                changed = true;
                actionText = String.format("Increased price for %s by ₹1 to ₹%s due to HIGH DEMAND (Score: %.1f, Stock Pressure: %.1f%%).", product.getFlavour(), targetPrice, demandScore, stockPct);
            } else {
                actionText = String.format("High demand detected for %s (Score: %.1f), but price is already capped at MAX limit of ₹%s.", product.getFlavour(), demandScore, maxPrice);
            }
        } else if (demandScore <= 35.0) {
            if (currentPrice.compareTo(minPrice) > 0) {
                targetPrice = currentPrice.subtract(BigDecimal.ONE); // - ₹1 step
                changed = true;
                actionText = String.format("Decreased price for %s by ₹1 to ₹%s due to LOW DEMAND (Score: %.1f, Stock Pressure: %.1f%%).", product.getFlavour(), targetPrice, demandScore, stockPct);
            } else {
                actionText = String.format("Low demand detected for %s (Score: %.1f), but price is already bounded at MIN floor of ₹%s.", product.getFlavour(), demandScore, minPrice);
            }
        } else {
            actionText = String.format("Price maintained at ₹%s for %s. Demand is steady (Score: %.1f, Stock Pressure: %.1f%%).", currentPrice, product.getFlavour(), demandScore, stockPct);
        }

        if (changed) {
            product.setCurrentCupPrice(targetPrice);
            product.setLastPriceChangeTimestamp(now);
            productRepository.save(product);

            PriceHistory history = PriceHistory.builder()
                    .productId(productId)
                    .oldPrice(currentPrice)
                    .newPrice(targetPrice)
                    .demandScore(demandScore)
                    .stockPressurePct(stockPct)
                    .timeFactorMultiplier(timeMult)
                    .explanation(actionText)
                    .createdAt(now)
                    .build();
            priceHistoryRepository.save(history);
        }

        return PriceEvaluationResult.builder()
                .productId(productId)
                .flavour(product.getFlavour())
                .oldPrice(currentPrice)
                .newPrice(targetPrice)
                .priceChanged(changed)
                .demandScore(demandScore)
                .stockPressurePct(stockPct)
                .timeFactorMultiplier(timeMult)
                .explanation(actionText)
                .statusReason(changed ? "PRICE_ADJUSTED" : "PRICE_STABLE")
                .build();
    }

    @Transactional
    public List<PriceEvaluationResult> evaluateAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(p -> evaluateAndAdjustPrice(p.getId()))
                .toList();
    }

    private double getConfigDouble(String key, double defaultVal) {
        return systemConfigRepository.findById(key)
                .map(c -> Double.parseDouble(c.getConfigValue()))
                .orElse(defaultVal);
    }

    private long getConfigLong(String key, long defaultVal) {
        return systemConfigRepository.findById(key)
                .map(c -> Long.parseLong(c.getConfigValue()))
                .orElse(defaultVal);
    }
}
