package com.retailpos.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "juice_batches")
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
    private Integer containerCapacityMl = 20000; // 20 Litres

    @Column(name = "initial_volume_ml", nullable = false)
    private Integer initialVolumeMl = 20000;

    @Column(name = "remaining_volume_ml", nullable = false)
    private Integer remainingVolumeMl = 20000;

    @Column(name = "cup_size_ml", nullable = false)
    private Integer cupSizeMl = 250;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchStatus status = BatchStatus.ACTIVE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public JuiceBatch() {}

    public JuiceBatch(Long id, Long productId, String batchCode, Integer containerCapacityMl, Integer initialVolumeMl, Integer remainingVolumeMl, Integer cupSizeMl, BatchStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.batchCode = batchCode;
        this.containerCapacityMl = containerCapacityMl != null ? containerCapacityMl : 20000;
        this.initialVolumeMl = initialVolumeMl != null ? initialVolumeMl : 20000;
        this.remainingVolumeMl = remainingVolumeMl != null ? remainingVolumeMl : 20000;
        this.cupSizeMl = cupSizeMl != null ? cupSizeMl : 250;
        this.status = status != null ? status : BatchStatus.ACTIVE;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public Integer getContainerCapacityMl() { return containerCapacityMl; }
    public void setContainerCapacityMl(Integer containerCapacityMl) { this.containerCapacityMl = containerCapacityMl; }
    public Integer getInitialVolumeMl() { return initialVolumeMl; }
    public void setInitialVolumeMl(Integer initialVolumeMl) { this.initialVolumeMl = initialVolumeMl; }
    public Integer getRemainingVolumeMl() { return remainingVolumeMl; }
    public void setRemainingVolumeMl(Integer remainingVolumeMl) { this.remainingVolumeMl = remainingVolumeMl; }
    public Integer getCupSizeMl() { return cupSizeMl; }
    public void setCupSizeMl(Integer cupSizeMl) { this.cupSizeMl = cupSizeMl; }
    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

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

    public static JuiceBatchBuilder builder() { return new JuiceBatchBuilder(); }

    public static class JuiceBatchBuilder {
        private Long id;
        private Long productId;
        private String batchCode;
        private Integer containerCapacityMl = 20000;
        private Integer initialVolumeMl = 20000;
        private Integer remainingVolumeMl = 20000;
        private Integer cupSizeMl = 250;
        private BatchStatus status = BatchStatus.ACTIVE;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public JuiceBatchBuilder id(Long id) { this.id = id; return this; }
        public JuiceBatchBuilder productId(Long productId) { this.productId = productId; return this; }
        public JuiceBatchBuilder batchCode(String batchCode) { this.batchCode = batchCode; return this; }
        public JuiceBatchBuilder containerCapacityMl(Integer containerCapacityMl) { this.containerCapacityMl = containerCapacityMl; return this; }
        public JuiceBatchBuilder initialVolumeMl(Integer initialVolumeMl) { this.initialVolumeMl = initialVolumeMl; return this; }
        public JuiceBatchBuilder remainingVolumeMl(Integer remainingVolumeMl) { this.remainingVolumeMl = remainingVolumeMl; return this; }
        public JuiceBatchBuilder cupSizeMl(Integer cupSizeMl) { this.cupSizeMl = cupSizeMl; return this; }
        public JuiceBatchBuilder status(BatchStatus status) { this.status = status; return this; }
        public JuiceBatchBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public JuiceBatchBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public JuiceBatch build() {
            return new JuiceBatch(id, productId, batchCode, containerCapacityMl, initialVolumeMl, remainingVolumeMl, cupSizeMl, status, createdAt, updatedAt);
        }
    }
}
