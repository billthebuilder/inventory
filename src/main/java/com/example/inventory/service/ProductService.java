package com.example.inventory.service;

import com.example.inventory.model.Product;
import com.example.inventory.messaging.InventoryEvent;
import com.example.inventory.messaging.PublisherService;
import com.example.inventory.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final PublisherService publisherService;

    public ProductService(ProductRepository productRepository, PublisherService publisherService) {
        this.productRepository = productRepository;
        this.publisherService = publisherService;
    }

    public Page<Product> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByDeletedFalse(pageable);
    }

    public Page<Product> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.search(q, pageable);
    }

    @Transactional
    public Product create(Product p) {
        Product saved = productRepository.save(p);
        publisherService.publishInventoryEvent(new InventoryEvent("PRODUCT_CREATED", String.valueOf(saved.getId())));
        return saved;
    }

    public Optional<Product> get(Long id) {
        return productRepository.findById(id).filter(p -> !p.isDeleted());
    }

    @Transactional
    public Optional<Product> update(Long id, Product update) {
        return productRepository.findById(id).filter(p -> !p.isDeleted()).map(p -> {
            p.setName(update.getName());
            p.setDescription(update.getDescription());
            p.setPrice(update.getPrice());
            p.setQuantity(update.getQuantity());
            p.setCategory(update.getCategory());
            p.setTags(update.getTags());
            Product saved = productRepository.save(p);
            publisherService.publishInventoryEvent(new InventoryEvent("PRODUCT_UPDATED", String.valueOf(saved.getId())));
            return saved;
        });
    }

    @Transactional
    public boolean softDelete(Long id) {
        return productRepository.findById(id).filter(p -> !p.isDeleted()).map(p -> {
            p.setDeleted(true);
            productRepository.save(p);
            publisherService.publishInventoryEvent(new InventoryEvent("PRODUCT_DELETED", String.valueOf(id)));
            return true;
        }).orElse(false);
    }

    @Transactional
    public Optional<Product> adjustQuantity(Long id, int delta) {
        return productRepository.findById(id).filter(p -> !p.isDeleted()).map(p -> {
            int newQty = Math.max(0, p.getQuantity() + delta);
            p.setQuantity(newQty);
            Product saved = productRepository.save(p);
            publisherService.publishInventoryEvent(new InventoryEvent("STOCK_ADJUSTED", String.valueOf(saved.getId())));
            return saved;
        });
    }
}
