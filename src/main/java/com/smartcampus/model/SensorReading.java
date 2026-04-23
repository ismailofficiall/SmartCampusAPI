package com.smartcampus.model;

import java.util.UUID;

public class SensorReading {
    private String id;
    private long timestamp;
    private double value;

    public SensorReading() {}

    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString(); // Auto-generate a unique ID
        this.timestamp = System.currentTimeMillis(); // Auto-capture the current time
        this.value = value;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}