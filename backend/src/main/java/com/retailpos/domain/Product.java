package com.retailpos.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String flavour;

    private String description;

    @Column(name = "default_cup_size_ml", nullable = false)
    @Builder.Default
    private Integer defaultCupSizeMl = 250;

    @Column(name = "default_cup_price", nullable = false)
    @Builder.Default
    private BigDecimal defaultCupPrice = new BigDecimal("20.00");

    @Column(name = "current_cup_price", nullable = false)
    @Builder.Default
    private BigDecimal currentCupPrice = new BigDecimal("20.00");

    @Column(name = "min_cup_price", nullable = false)
    @Builder.Default
    private BigDecimal minCupPrice = new BigDecimal("18.00");

    @Column(name = "max_cup_price", nullable = false)
    @Builder.Default
    private BigDecimal maxCupPrice = new BigDecimal("25.00");

    @Column(name = "last_price_change_timestamp")
    private LocalDateTime lastPriceChangeTimestamp;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
