package com.example.inventory.api;

import com.example.inventory.model.Product;
import com.example.inventory.service.ProductService;
import com.example.inventory.messaging.PublisherService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final PublisherService publisherService;

    public ProductController(ProductService productService, PublisherService publisherService) {
        this.productService = productService;
        this.publisherService = publisherService;
    }

    @GetMapping
    public Page<Product> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return productService.list(page, size);
    }

    @GetMapping("/search")
    public Page<Product> search(@RequestParam(required = false) String q,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size) {
        return productService.search(q, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        Optional<Product> p = productService.get(id);
        return p.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody Product p) {
        Product saved = productService.create(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @Valid @RequestBody Product p) {
        return productService.update(id, p)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean ok = productService.softDelete(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/adjust")
    public ResponseEntity<Product> adjust(@PathVariable Long id, @RequestParam int delta) {
        return productService.adjustQuantity(id, delta)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CSV fields: name,description,price,quantity,category,tags
    @PostMapping("/bulk-upload")
    public ResponseEntity<String> bulkUpload(@RequestParam("file") MultipartFile file) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                publisherService.publishBulkUploadLine(line);
            }
            return ResponseEntity.accepted().body("Bulk upload accepted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid CSV: " + e.getMessage());
        }
    }
}
