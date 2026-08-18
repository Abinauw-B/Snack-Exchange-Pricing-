package com.retailpos.inventory;

import com.retailpos.domain.JuiceBatch;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@CrossOrigin(origins = "*")
public class JuiceBatchController {

    private final JuiceBatchService juiceBatchService;

    public JuiceBatchController(JuiceBatchService juiceBatchService) {
        this.juiceBatchService = juiceBatchService;
    }

    @GetMapping
    public ResponseEntity<List<JuiceBatch>> getAllBatches() {
        return ResponseEntity.ok(juiceBatchService.getAllBatches());
    }

    @GetMapping("/active")
    public ResponseEntity<List<JuiceBatch>> getActiveBatches() {
        return ResponseEntity.ok(juiceBatchService.getActiveBatches());
    }

    public static class CreateBatchRequest {
        private Long productId;
        private Integer containerCapacityMl;

        public CreateBatchRequest() {}
        public CreateBatchRequest(Long productId, Integer containerCapacityMl) {
            this.productId = productId;
            this.containerCapacityMl = containerCapacityMl;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getContainerCapacityMl() { return containerCapacityMl; }
        public void setContainerCapacityMl(Integer containerCapacityMl) { this.containerCapacityMl = containerCapacityMl; }
    }

    @PostMapping
    public ResponseEntity<JuiceBatch> registerBatch(@RequestBody CreateBatchRequest request) {
        JuiceBatch newBatch = juiceBatchService.registerNewBatch(request.getProductId(), request.getContainerCapacityMl());
        return ResponseEntity.ok(newBatch);
    }
}
