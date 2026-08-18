package com.retailpos.inventory;

import com.retailpos.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class JuiceBatchService {

    private final JuiceBatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    public JuiceBatchService(JuiceBatchRepository batchRepository, ProductRepository productRepository, InventoryTransactionRepository transactionRepository) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<JuiceBatch> getAllBatches() {
        return batchRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<JuiceBatch> getActiveBatches() {
        return batchRepository.findByStatus(JuiceBatch.BatchStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public JuiceBatch getActiveBatchForProduct(Long productId) {
        return batchRepository.findFirstActiveBatchForProduct(productId)
                .orElse(null);
    }

    @Transactional
    public JuiceBatch registerNewBatch(Long productId, Integer containerCapacityMl) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        int capacity = (containerCapacityMl != null && containerCapacityMl > 0) ? containerCapacityMl : 20000;
        String batchCode = "BATCH-" + product.getFlavour().substring(0, Math.min(3, product.getFlavour().length())).toUpperCase()
                + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        JuiceBatch batch = JuiceBatch.builder()
                .productId(productId)
                .batchCode(batchCode)
                .containerCapacityMl(capacity)
                .initialVolumeMl(capacity)
                .remainingVolumeMl(capacity)
                .cupSizeMl(product.getDefaultCupSizeMl())
                .status(JuiceBatch.BatchStatus.ACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();

        JuiceBatch savedBatch = batchRepository.save(batch);

        // Record inventory transaction
        InventoryTransaction tx = InventoryTransaction.builder()
                .productId(productId)
                .batchId(savedBatch.getId())
                .transactionType("BATCH_CREATED")
                .volumeChangeMl(capacity)
                .notes("Registered new 20L juice container batch: " + batchCode)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        return savedBatch;
    }

    @Transactional
    public JuiceBatch deductBatchVolume(Long productId, int mlToDeduct) {
        List<JuiceBatch> activeBatches = batchRepository.findActiveBatchesForProductWithLock(productId);
        if (activeBatches.isEmpty()) {
            throw new IllegalStateException("No active juice batch available for product ID: " + productId);
        }

        JuiceBatch activeBatch = activeBatches.get(0);
        activeBatch.deductVolume(mlToDeduct);

        JuiceBatch updatedBatch = batchRepository.save(activeBatch);

        // Log transaction
        InventoryTransaction tx = InventoryTransaction.builder()
                .productId(productId)
                .batchId(updatedBatch.getId())
                .transactionType("POS_SALE")
                .volumeChangeMl(-mlToDeduct)
                .notes("Deducted " + mlToDeduct + " ml for sale. Remaining: " + updatedBatch.getRemainingVolumeMl() + " ml")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        return updatedBatch;
    }
}
