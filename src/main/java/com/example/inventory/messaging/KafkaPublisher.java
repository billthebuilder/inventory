package com.example.inventory.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPublisher implements PublisherService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String inventoryTopic;
    private final String auditTopic;
    private final String bulkTopic;

    public KafkaPublisher(KafkaTemplate<String, String> kafkaTemplate,
                          ObjectMapper objectMapper,
                          @Value("${inventory.kafka.topics.inventory-events}") String inventoryTopic,
                          @Value("${inventory.kafka.topics.audit}") String auditTopic,
                          @Value("${inventory.kafka.topics.bulk-upload}") String bulkTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.inventoryTopic = inventoryTopic;
        this.auditTopic = auditTopic;
        this.bulkTopic = bulkTopic;
    }

    @Override
    public void publishInventoryEvent(InventoryEvent event) {
        try {
            kafkaTemplate.send(inventoryTopic, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize inventory event", e);
        }
    }

    @Override
    public void publishAudit(String message) {
        kafkaTemplate.send(auditTopic, message);
    }

    @Override
    public void publishBulkUploadLine(String csvLine) {
        kafkaTemplate.send(bulkTopic, csvLine);
    }
}
