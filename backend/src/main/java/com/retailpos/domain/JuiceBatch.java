package com.retailpos.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "juice_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JuiceBatch {

    public enum BatchStatus {
        ACTIVE, DEPLETED, EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "batch_code", nullable = false, unique = true, length = 50)
    private String batchCode;

    @Column(name = "container_capacity_ml", nullable = false)
    @Builder.Default
    private Integer containerCapacityMl = 20000; // 20 Litres

    @Column(name = "initial_volume_ml", nullable = false)
    @Builder.Default
    private Integer initialVolumeMl = 20000;

    @Column(name = "remaining_volume_ml", nullable = false)
    @Builder.Default
    private Integer remainingVolumeMl = 20000;

    @Column(name = "cup_size_ml", nullable = false)
    @Builder.Default
    private Integer cupSizeMl = 250;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BatchStatus status = BatchStatus.ACTIVE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public int getEstimatedRemainingCups() {
        if (cupSizeMl == null || cupSizeMl <= 0 || remainingVolumeMl == null) {
            return 0;
        }
        return (int) Math.floor((double) remainingVolumeMl / cupSizeMl);
    }

    public synchronized void deductVolume(int volumeMl) {
        if (volumeMl <= 0) {
            throw new IllegalArgumentException("Volume to deduct must be greater than zero");
        }
        if (this.remainingVolumeMl < volumeMl) {
            throw new IllegalStateException("Insufficient volume remaining in batch. Requested: " 
                + volumeMl + " ml, Available: " + this.remainingVolumeMl + " ml");
        }
        this.remainingVolumeMl -= volumeMl;
        this.updatedAt = LocalDateTime.now();
        if (this.remainingVolumeMl == 0) {
            this.status = BatchStatus.DEPLETED;
        }
    }
}
