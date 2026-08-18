package com.retailpos.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "old_price", nullable = false)
    private BigDecimal oldPrice;

    @Column(name = "new_price", nullable = false)
    private BigDecimal newPrice;

    @Column(name = "demand_score", nullable = false)
    private Double demandScore;

    @Column(name = "stock_pressure_pct", nullable = false)
    private Double stockPressurePct;

    @Column(name = "time_factor_multiplier", nullable = false)
    private Double timeFactorMultiplier;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String explanation;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public PriceHistory() {}

    public PriceHistory(Long id, Long productId, BigDecimal oldPrice, BigDecimal newPrice, Double demandScore, Double stockPressurePct, Double timeFactorMultiplier, String explanation, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.demandScore = demandScore;
        this.stockPressurePct = stockPressurePct;
        this.timeFactorMultiplier = timeFactorMultiplier;
        this.explanation = explanation;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; }
    public BigDecimal getNewPrice() { return newPrice; }
    public void setNewPrice(BigDecimal newPrice) { this.newPrice = newPrice; }
    public Double getDemandScore() { return demandScore; }
    public void setDemandScore(Double demandScore) { this.demandScore = demandScore; }
    public Double getStockPressurePct() { return stockPressurePct; }
    public void setStockPressurePct(Double stockPressurePct) { this.stockPressurePct = stockPressurePct; }
    public Double getTimeFactorMultiplier() { return timeFactorMultiplier; }
    public void setTimeFactorMultiplier(Double timeFactorMultiplier) { this.timeFactorMultiplier = timeFactorMultiplier; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static PriceHistoryBuilder builder() { return new PriceHistoryBuilder(); }

    public static class PriceHistoryBuilder {
        private Long id;
        private Long productId;
        private BigDecimal oldPrice;
        private BigDecimal newPrice;
        private Double demandScore;
        private Double stockPressurePct;
        private Double timeFactorMultiplier;
        private String explanation;
        private LocalDateTime createdAt = LocalDateTime.now();

        public PriceHistoryBuilder id(Long id) { this.id = id; return this; }
        public PriceHistoryBuilder productId(Long productId) { this.productId = productId; return this; }
        public PriceHistoryBuilder oldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; return this; }
        public PriceHistoryBuilder newPrice(BigDecimal newPrice) { this.newPrice = newPrice; return this; }
        public PriceHistoryBuilder demandScore(Double demandScore) { this.demandScore = demandScore; return this; }
        public PriceHistoryBuilder stockPressurePct(Double stockPressurePct) { this.stockPressurePct = stockPressurePct; return this; }
        public PriceHistoryBuilder timeFactorMultiplier(Double timeFactorMultiplier) { this.timeFactorMultiplier = timeFactorMultiplier; return this; }
        public PriceHistoryBuilder explanation(String explanation) { this.explanation = explanation; return this; }
        public PriceHistoryBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public PriceHistory build() {
            return new PriceHistory(id, productId, oldPrice, newPrice, demandScore, stockPressurePct, timeFactorMultiplier, explanation, createdAt);
        }
    }
}
