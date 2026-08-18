package com.retailpos.pos;

import com.retailpos.domain.PriceHistory;
import com.retailpos.domain.PriceHistoryRepository;
import com.retailpos.domain.Product;
import com.retailpos.domain.ProductRepository;
import com.retailpos.domain.SalesOrder;
import com.retailpos.domain.SalesOrderRepository;
import com.retailpos.inventory.JuiceBatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/pos", "/api"})
@CrossOrigin(origins = "*")
public class POSController {

    private final POSService posService;
    private final ProductRepository productRepository;
    private final JuiceBatchService juiceBatchService;
    private final SalesOrderRepository salesOrderRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public POSController(POSService posService, ProductRepository productRepository, JuiceBatchService juiceBatchService, SalesOrderRepository salesOrderRepository, PriceHistoryRepository priceHistoryRepository) {
        this.posService = posService;
        this.productRepository = productRepository;
        this.juiceBatchService = juiceBatchService;
        this.salesOrderRepository = salesOrderRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAvailableProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/{id}/price")
    public ResponseEntity<Map<String, Object>> getProductPrice(@PathVariable Long id) {
        return productRepository.findById(id).map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", p.getId());
            map.put("productName", p.getName());
            map.put("flavour", p.getFlavour());
            map.put("currentCupPrice", p.getCurrentCupPrice());
            map.put("minCupPrice", p.getMinCupPrice());
            map.put("maxCupPrice", p.getMaxCupPrice());
            map.put("lastUpdated", p.getLastPriceChangeTimestamp());
            return ResponseEntity.ok(map);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/{id}/price-history")
    public ResponseEntity<List<PriceHistory>> getProductPriceHistory(@PathVariable Long id) {
        return ResponseEntity.ok(priceHistoryRepository.findByProductIdOrderByCreatedAtDesc(id));
    }

    public static class StockUpdateRequest {
        private Integer volumeMl;
        public Integer getVolumeMl() { return volumeMl; }
        public void setVolumeMl(Integer volumeMl) { this.volumeMl = volumeMl; }
    }

    @PutMapping("/products/{id}/stock")
    public ResponseEntity<Map<String, Object>> updateStock(@PathVariable Long id, @RequestBody StockUpdateRequest req) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        int volume = (req != null && req.getVolumeMl() != null) ? req.getVolumeMl() : 20000;
        juiceBatchService.registerNewBatch(id, volume);
        Map<String, Object> res = new HashMap<>();
        res.put("productId", id);
        res.put("addedVolumeMl", volume);
        res.put("message", "Stock refilled successfully");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        if (product.getCurrentCupPrice() == null) {
            product.setCurrentCupPrice(product.getMinCupPrice() != null ? product.getMinCupPrice() : BigDecimal.valueOf(25));
        }
        if (product.getDefaultCupSizeMl() == null) {
            product.setDefaultCupSizeMl(250);
        }
        if (product.getMinCupPrice() == null) {
            product.setMinCupPrice(BigDecimal.valueOf(18));
        }
        if (product.getMaxCupPrice() == null) {
            product.setMaxCupPrice(BigDecimal.valueOf(35));
        }
        product.setLastPriceChangeTimestamp(LocalDateTime.now());
        Product saved = productRepository.save(product);

        // Auto-register initial 20L container batch for new product
        try {
            juiceBatchService.registerNewBatch(saved.getId(), 20000);
        } catch (Exception e) {}

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product details) {
        return productRepository.findById(id).map(existing -> {
            if (details.getName() != null) existing.setName(details.getName());
            if (details.getFlavour() != null) existing.setFlavour(details.getFlavour());
            if (details.getDescription() != null) existing.setDescription(details.getDescription());
            if (details.getCurrentCupPrice() != null) existing.setCurrentCupPrice(details.getCurrentCupPrice());
            if (details.getMinCupPrice() != null) existing.setMinCupPrice(details.getMinCupPrice());
            if (details.getMaxCupPrice() != null) existing.setMaxCupPrice(details.getMaxCupPrice());
            if (details.getDefaultCupPrice() != null) existing.setDefaultCupPrice(details.getDefaultCupPrice());
            existing.setLastPriceChangeTimestamp(LocalDateTime.now());
            return ResponseEntity.ok(productRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping({"/checkout", "/orders"})
    public ResponseEntity<POSService.CheckoutResponse> checkout(@RequestBody POSService.CheckoutRequest request) {
        POSService.CheckoutResponse response = posService.processCheckout(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<SalesOrder>> getAllOrders() {
        return ResponseEntity.ok(salesOrderRepository.findAll());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<SalesOrder> getOrderById(@PathVariable Long id) {
        return salesOrderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
