package com.example.inventory.messaging;

import lombok.Data;

import java.time.Instant;
@Data
public class InventoryEvent {
    private String type;
    private String payload;
    private Instant timestamp = Instant.now();

    public InventoryEvent(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }
}
