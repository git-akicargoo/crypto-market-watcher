package com.example.boot_redis_kafka_mysql.crypto.exchange.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TickerData {
    private String exchange;
    private String symbol;
    private double price;
    private double volume;
    private long timestamp;
} 