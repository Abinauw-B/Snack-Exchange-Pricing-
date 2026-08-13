package com.retailpos.pricing;

import com.retailpos.domain.Product;
import com.retailpos.domain.ProductRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceLockService {

    private final ProductRepository productRepository;
    private final Map<String, LockedPriceVersion> lockRegistry = new ConcurrentHashMap<>();

    @Data
    @Builder
    public static class LockedPriceVersion {
        private String lockToken;
        private Long productId;
        private String productName;
        private BigDecimal lockedPrice;
        private int priceVersion;
        private LocalDateTime lockedAt;
        private LocalDateTime expiresAt;
        private boolean redeemed;
    }

    @Transactional(readOnly = true)
    public LockedPriceVersion lockPriceForCartItem(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        String token = "LOCK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusSeconds(120); // 120-second price guarantee lock

        LockedPriceVersion lock = LockedPriceVersion.builder()
                .lockToken(token)
                .productId(product.getId())
                .productName(product.getName())
                .lockedPrice(product.getCurrentCupPrice())
                .priceVersion((int) (System.currentTimeMillis() / 1000))
                .lockedAt(now)
                .expiresAt(expires)
                .redeemed(false)
                .build();

        lockRegistry.put(token, lock);
        log.info("🔒 Price Locked: Token {} for Product {} at ₹{}", token, product.getName(), product.getCurrentCupPrice());

        return lock;
    }

    public LockedPriceVersion validateAndRedeemLock(String lockToken) {
        LockedPriceVersion lock = lockRegistry.get(lockToken);
        if (lock == null) {
            throw new IllegalArgumentException("Invalid price lock token: " + lockToken);
        }

        if (LocalDateTime.now().isAfter(lock.getExpiresAt())) {
            lockRegistry.remove(lockToken);
            throw new IllegalStateException("Price lock token expired at " + lock.getExpiresAt());
        }

        lock.setRedeemed(true);
        lockRegistry.remove(lockToken);
        return lock;
    }
}
