package com.example.inventory.messaging;

import com.example.inventory.model.Product;
import com.example.inventory.repository.ProductRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class BulkUploadListener {
    private final ProductRepository productRepository;
    private final PublisherService publisherService;

    public BulkUploadListener(ProductRepository productRepository, PublisherService publisherService) {
        this.productRepository = productRepository;
        this.publisherService = publisherService;
    }

    // CSV fields: name,description,price,quantity,category,tags
    @KafkaListener(topics = "${inventory.kafka.topics.bulk-upload}")
    @Transactional
    public void onCsvLine(String line) {
        try {
            String[] parts = line.split(",", -1);
            if (parts.length < 6) return;
            Product p = new Product();
            p.setName(parts[0].trim());
            p.setDescription(parts[1].trim());
            p.setPrice(new BigDecimal(parts[2].trim()));
            p.setQuantity(Integer.parseInt(parts[3].trim()));
            p.setCategory(parts[4].trim());
            p.setTags(parts[5].trim());
            Product saved = productRepository.save(p);
            publisherService.publishInventoryEvent(new InventoryEvent("PRODUCT_BULK_CREATED", String.valueOf(saved.getId())));
        } catch (Exception e) {
            publisherService.publishAudit("Bulk upload line failed: " + e.getMessage());
        }
    }
}
