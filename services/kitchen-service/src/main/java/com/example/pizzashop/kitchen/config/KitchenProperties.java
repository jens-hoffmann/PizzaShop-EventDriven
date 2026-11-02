package com.example.pizzashop.kitchen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kitchen")
public class KitchenProperties {

    private long acceptDelayMs = 1_000;
    private long preparationDelayMs = 4_000;
    private long pollIntervalMs = 500;
    private int batchSize = 50;

    public long getAcceptDelayMs() {
        return acceptDelayMs;
    }

    public void setAcceptDelayMs(long acceptDelayMs) {
        this.acceptDelayMs = acceptDelayMs;
    }

    public long getPreparationDelayMs() {
        return preparationDelayMs;
    }

    public void setPreparationDelayMs(long preparationDelayMs) {
        this.preparationDelayMs = preparationDelayMs;
    }

    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
