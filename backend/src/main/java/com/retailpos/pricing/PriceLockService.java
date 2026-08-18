package com.retailpos.pricing;

import com.retailpos.domain.Product;
import com.retailpos.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PriceLockService {

    private static final Logger log = LoggerFactory.getLogger(PriceLockService.class);

    private final ProductRepository productRepository;
    private final Map<String, LockedPriceVersion> lockRegistry = new ConcurrentHashMap<>();

    public PriceLockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public static class LockedPriceVersion {
        private String lockToken;
        private Long productId;
        private String productName;
        private BigDecimal lockedPrice;
        private int priceVersion;
        private LocalDateTime lockedAt;
        private LocalDateTime expiresAt;
        private boolean redeemed;

        public LockedPriceVersion() {}
        public LockedPriceVersion(String lockToken, Long productId, String productName, BigDecimal lockedPrice, int priceVersion, LocalDateTime lockedAt, LocalDateTime expiresAt, boolean redeemed) {
            this.lockToken = lockToken;
            this.productId = productId;
            this.productName = productName;
            this.lockedPrice = lockedPrice;
            this.priceVersion = priceVersion;
            this.lockedAt = lockedAt;
            this.expiresAt = expiresAt;
            this.redeemed = redeemed;
        }

        public String getLockToken() { return lockToken; }
        public void setLockToken(String lockToken) { this.lockToken = lockToken; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public BigDecimal getLockedPrice() { return lockedPrice; }
        public void setLockedPrice(BigDecimal lockedPrice) { this.lockedPrice = lockedPrice; }
        public int getPriceVersion() { return priceVersion; }
        public void setPriceVersion(int priceVersion) { this.priceVersion = priceVersion; }
        public LocalDateTime getLockedAt() { return lockedAt; }
        public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        public boolean isRedeemed() { return redeemed; }
        public void setRedeemed(boolean redeemed) { this.redeemed = redeemed; }

        public static LockedPriceVersionBuilder builder() { return new LockedPriceVersionBuilder(); }
        public static class LockedPriceVersionBuilder {
            private String lockToken;
            private Long productId;
            private String productName;
            private BigDecimal lockedPrice;
            private int priceVersion;
            private LocalDateTime lockedAt;
            private LocalDateTime expiresAt;
            private boolean redeemed;

            public LockedPriceVersionBuilder lockToken(String lockToken) { this.lockToken = lockToken; return this; }
            public LockedPriceVersionBuilder productId(Long productId) { this.productId = productId; return this; }
            public LockedPriceVersionBuilder productName(String productName) { this.productName = productName; return this; }
            public LockedPriceVersionBuilder lockedPrice(BigDecimal lockedPrice) { this.lockedPrice = lockedPrice; return this; }
            public LockedPriceVersionBuilder priceVersion(int priceVersion) { this.priceVersion = priceVersion; return this; }
            public LockedPriceVersionBuilder lockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; return this; }
            public LockedPriceVersionBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
            public LockedPriceVersionBuilder redeemed(boolean redeemed) { this.redeemed = redeemed; return this; }
            public LockedPriceVersion build() { return new LockedPriceVersion(lockToken, productId, productName, lockedPrice, priceVersion, lockedAt, expiresAt, redeemed); }
        }
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
