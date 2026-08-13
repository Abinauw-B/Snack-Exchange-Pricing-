package com.retailpos.pricing;

import com.retailpos.domain.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketCrashService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    private boolean crashActive = false;
    private LocalDateTime crashEndTime;

    @Data
    @Builder
    public static class MarketCrashStatus {
        private boolean active;
        private long remainingSeconds;
        private LocalDateTime endTime;
        private String message;
    }

    public synchronized MarketCrashStatus getStatus() {
        if (crashActive && LocalDateTime.now().isAfter(crashEndTime)) {
            crashActive = false;
        }

        long remaining = 0;
        if (crashActive && crashEndTime != null) {
            remaining = Math.max(0, crashEndTime.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        }

        return MarketCrashStatus.builder()
                .active(crashActive)
                .remainingSeconds(remaining)
                .endTime(crashEndTime)
                .message(crashActive ? "🚨 MARKET CRASH IN PROGRESS! All juices set to floor price!" : "Trading normal. Dynamic price algorithm active.")
                .build();
    }

    @Transactional
    public synchronized MarketCrashStatus triggerMarketCrash(int durationMinutes) {
        int duration = (durationMinutes > 0) ? durationMinutes : 3;
        this.crashActive = true;
        this.crashEndTime = LocalDateTime.now().plusMinutes(duration);

        List<Product> products = productRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Product product : products) {
            BigDecimal oldPrice = product.getCurrentCupPrice();
            BigDecimal floorPrice = product.getMinCupPrice();

            product.setCurrentCupPrice(floorPrice);
            product.setLastPriceChangeTimestamp(now);
            productRepository.save(product);

            PriceHistory history = PriceHistory.builder()
                    .productId(product.getId())
                    .oldPrice(oldPrice)
                    .newPrice(floorPrice)
                    .demandScore(0.0)
                    .stockPressurePct(0.0)
                    .timeFactorMultiplier(1.0)
                    .explanation(String.format("🚨 MARKET CRASH TRIGGERED! Price dropped from ₹%s to absolute floor limit ₹%s for %s.", oldPrice, floorPrice, product.getFlavour()))
                    .createdAt(now)
                    .build();
            priceHistoryRepository.save(history);
        }

        return getStatus();
    }

    public synchronized MarketCrashStatus stopMarketCrash() {
        this.crashActive = false;
        this.crashEndTime = LocalDateTime.now();
        return getStatus();
    }

    public boolean isCrashActive() {
        if (crashActive && LocalDateTime.now().isAfter(crashEndTime)) {
            crashActive = false;
        }
        return crashActive;
    }
}
