package com.example.inventory.messaging;

public interface PublisherService {
    void publishInventoryEvent(InventoryEvent event);
    void publishAudit(String message);
    void publishBulkUploadLine(String csvLine);
}
