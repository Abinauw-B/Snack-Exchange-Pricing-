package com.retailpos.pricing;

import com.retailpos.domain.PriceHistory;
import com.retailpos.domain.PriceHistoryRepository;
import com.retailpos.domain.SystemConfig;
import com.retailpos.domain.SystemConfigRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PricingController {

    private final PriceAdjustmentService priceAdjustmentService;
    private final PricingSimulationService pricingSimulationService;
    private final PriceHistoryRepository priceHistoryRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final MarketCrashService marketCrashService;

    @GetMapping("/market-crash/status")
    public ResponseEntity<MarketCrashService.MarketCrashStatus> getMarketCrashStatus() {
        return ResponseEntity.ok(marketCrashService.getStatus());
    }

    @PostMapping("/market-crash/trigger")
    public ResponseEntity<MarketCrashService.MarketCrashStatus> triggerMarketCrash(@RequestParam(defaultValue = "3") int durationMinutes) {
        return ResponseEntity.ok(marketCrashService.triggerMarketCrash(durationMinutes));
    }

    @PostMapping("/market-crash/stop")
    public ResponseEntity<MarketCrashService.MarketCrashStatus> stopMarketCrash() {
        return ResponseEntity.ok(marketCrashService.stopMarketCrash());
    }

    @GetMapping("/evaluate")
    public ResponseEntity<List<PriceAdjustmentService.PriceEvaluationResult>> evaluateAllPrices() {
        return ResponseEntity.ok(priceAdjustmentService.evaluateAllProducts());
    }

    @PostMapping("/evaluate/{productId}")
    public ResponseEntity<PriceAdjustmentService.PriceEvaluationResult> evaluateProductPrice(@PathVariable Long productId) {
        return ResponseEntity.ok(priceAdjustmentService.evaluateAndAdjustPrice(productId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PriceHistory>> getPriceHistory() {
        return ResponseEntity.ok(priceHistoryRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/products/{productId}/history")
    public ResponseEntity<List<PriceHistory>> getProductPriceHistory(@PathVariable Long productId) {
        return ResponseEntity.ok(priceHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @PostMapping("/simulate")
    public ResponseEntity<PricingSimulationService.SimulationResponse> simulatePricing(@RequestBody PricingSimulationService.SimulationRequest request) {
        return ResponseEntity.ok(pricingSimulationService.runSimulation(request));
    }

    @GetMapping("/config")
    public ResponseEntity<List<SystemConfig>> getConfig() {
        return ResponseEntity.ok(systemConfigRepository.findAll());
    }

    @Data
    public static class UpdateConfigItem {
        private String key;
        private String value;
    }

    @PutMapping("/config")
    public ResponseEntity<List<SystemConfig>> updateConfig(@RequestBody List<UpdateConfigItem> updates) {
        for (UpdateConfigItem item : updates) {
            systemConfigRepository.findById(item.getKey()).ifPresent(cfg -> {
                cfg.setConfigValue(item.getValue());
                systemConfigRepository.save(cfg);
            });
        }
        return ResponseEntity.ok(systemConfigRepository.findAll());
    }
}
