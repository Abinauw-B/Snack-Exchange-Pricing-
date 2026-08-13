package com.retailpos.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
