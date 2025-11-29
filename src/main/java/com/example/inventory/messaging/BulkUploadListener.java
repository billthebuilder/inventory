package com.example.inventory.messaging;

import com.example.inventory.model.Product;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

@Component
@Slf4j
public class BulkUploadListener {
    private final ProductRepository productRepository;
    private final PublisherService publisherService;

    public BulkUploadListener(ProductRepository productRepository, PublisherService publisherService) {
        this.productRepository = productRepository;
        this.publisherService = publisherService;
    }

    // CSV fields: name,description,price,quantity,category,tags
    @KafkaListener(topics = "${inventory.kafka.topics.bulk-upload}")
    public void onCsvLine(String csvLine) {
        try {
            // Double-check to ensure header is skipped if it wasn't filtered by the producer
            if (csvLine.startsWith("name,description")) {
                return;
            }

            try (CSVParser csvParser = CSVParser.parse(csvLine, CSVFormat.DEFAULT)) {
                for (CSVRecord csvRecord : csvParser) {
                    if (csvRecord.size() < 6) continue;
                    String name = csvRecord.get(0);
                    String description = csvRecord.get(1);
                    String price = csvRecord.get(2);
                    String quantity = csvRecord.get(3);
                    String category = csvRecord.get(4);
                    String tags = csvRecord.get(5);

                    Product p = new Product();
                    p.setName(name);
                    p.setDescription(description);
                    p.setPrice(price);
                    p.setQuantity(Integer.parseInt(quantity));
                    p.setCategory(category);
                    p.setTags(tags);
                    Product saved = productRepository.save(p);
                    publisherService.publishInventoryEvent(new InventoryEvent("PRODUCT_BULK_CREATED", String.valueOf(saved.getId())));
                }
            }
        } catch (Exception e) {
            LogUtil.logException(log,e);
            publisherService.publishAudit("Bulk upload line failed: " + e.getMessage());
        }
    }

}
