package com.retailpos.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
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
    private LocalDateTime createdAt = LocalDateTime.now();

    public InventoryTransaction() {}

    public InventoryTransaction(Long id, Long productId, Long batchId, String transactionType, Integer volumeChangeMl, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.batchId = batchId;
        this.transactionType = transactionType;
        this.volumeChangeMl = volumeChangeMl;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public Integer getVolumeChangeMl() { return volumeChangeMl; }
    public void setVolumeChangeMl(Integer volumeChangeMl) { this.volumeChangeMl = volumeChangeMl; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static InventoryTransactionBuilder builder() { return new InventoryTransactionBuilder(); }

    public static class InventoryTransactionBuilder {
        private Long id;
        private Long productId;
        private Long batchId;
        private String transactionType;
        private Integer volumeChangeMl;
        private String notes;
        private LocalDateTime createdAt = LocalDateTime.now();

        public InventoryTransactionBuilder id(Long id) { this.id = id; return this; }
        public InventoryTransactionBuilder productId(Long productId) { this.productId = productId; return this; }
        public InventoryTransactionBuilder batchId(Long batchId) { this.batchId = batchId; return this; }
        public InventoryTransactionBuilder transactionType(String transactionType) { this.transactionType = transactionType; return this; }
        public InventoryTransactionBuilder volumeChangeMl(Integer volumeChangeMl) { this.volumeChangeMl = volumeChangeMl; return this; }
        public InventoryTransactionBuilder notes(String notes) { this.notes = notes; return this; }
        public InventoryTransactionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public InventoryTransaction build() {
            return new InventoryTransaction(id, productId, batchId, transactionType, volumeChangeMl, notes, createdAt);
        }
    }
}
