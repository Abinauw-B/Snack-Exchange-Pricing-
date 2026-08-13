package com.retailpos.inventory;

import com.retailpos.domain.JuiceBatch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JuiceBatchController {

    private final JuiceBatchService juiceBatchService;

    @GetMapping
    public ResponseEntity<List<JuiceBatch>> getAllBatches() {
        return ResponseEntity.ok(juiceBatchService.getAllBatches());
    }

    @GetMapping("/active")
    public ResponseEntity<List<JuiceBatch>> getActiveBatches() {
        return ResponseEntity.ok(juiceBatchService.getActiveBatches());
    }

    @Data
    public static class CreateBatchRequest {
        private Long productId;
        private Integer containerCapacityMl; // Defaults to 20,000ml (20L)
    }

    @PostMapping
    public ResponseEntity<JuiceBatch> registerBatch(@RequestBody CreateBatchRequest request) {
        JuiceBatch newBatch = juiceBatchService.registerNewBatch(request.getProductId(), request.getContainerCapacityMl());
        return ResponseEntity.ok(newBatch);
    }
}
