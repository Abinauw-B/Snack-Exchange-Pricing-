package com.retailpos.pos;

import com.retailpos.domain.Product;
import com.retailpos.domain.ProductRepository;
import com.retailpos.inventory.JuiceBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class POSController {

    private final POSService posService;
    private final ProductRepository productRepository;
    private final JuiceBatchService juiceBatchService;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAvailableProducts() {
        return ResponseEntity.ok(productRepository.findAll());
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

    @PostMapping("/checkout")
    public ResponseEntity<POSService.CheckoutResponse> checkout(@RequestBody POSService.CheckoutRequest request) {
        POSService.CheckoutResponse response = posService.processCheckout(request);
        return ResponseEntity.ok(response);
    }
}
