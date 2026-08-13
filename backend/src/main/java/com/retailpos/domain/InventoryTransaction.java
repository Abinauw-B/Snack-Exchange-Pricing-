package com.retailpos.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType; // BATCH_CREATED, POS_SALE, BATCH_DEPLETED, MANUAL_ADJUSTMENT

    @Column(name = "volume_change_ml", nullable = false)
    private Integer volumeChangeMl;

    private String notes;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
