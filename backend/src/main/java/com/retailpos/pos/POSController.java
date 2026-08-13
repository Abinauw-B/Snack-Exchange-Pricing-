package com.retailpos.pos;

import com.retailpos.domain.Product;
import com.retailpos.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class POSController {

    private final POSService posService;
    private final ProductRepository productRepository;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAvailableProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @PostMapping("/checkout")
    public ResponseEntity<POSService.CheckoutResponse> checkout(@RequestBody POSService.CheckoutRequest request) {
        POSService.CheckoutResponse response = posService.processCheckout(request);
        return ResponseEntity.ok(response);
    }
}
