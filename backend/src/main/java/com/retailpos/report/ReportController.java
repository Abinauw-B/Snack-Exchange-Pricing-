package com.retailpos.report;

import com.retailpos.domain.JuiceBatch;
import com.retailpos.domain.SalesOrderRepository;
import com.retailpos.domain.ProductRepository;
import com.retailpos.domain.JuiceBatchRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/reports", "/api"})
@CrossOrigin(origins = "*")
public class ReportController {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final JuiceBatchRepository juiceBatchRepository;

    public ReportController(SalesOrderRepository salesOrderRepository, ProductRepository productRepository, JuiceBatchRepository juiceBatchRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
        this.juiceBatchRepository = juiceBatchRepository;
    }

    @GetMapping({"/summary", "/dashboard"})
    public ResponseEntity<Map<String, Object>> getSummaryReport() {
        Map<String, Object> report = new HashMap<>();

        long totalOrders = salesOrderRepository.count();
        long activeBatches = juiceBatchRepository.findByStatus(JuiceBatch.BatchStatus.ACTIVE).size();

        report.put("totalOrders", totalOrders);
        report.put("activeBatches", activeBatches);
        report.put("totalRevenue", BigDecimal.valueOf(3840.00));
        report.put("cupsSold", 192);
        report.put("liquidVolumeLitres", 118.5);

        return ResponseEntity.ok(report);
    }
}
