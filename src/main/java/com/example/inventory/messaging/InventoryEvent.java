package com.example.inventory.messaging;

import java.time.Instant;

public class InventoryEvent {
    private String type;
    private String payload;
    private Instant timestamp = Instant.now();

    public InventoryEvent() {}

    public InventoryEvent(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
