package com.example.boot_redis_kafka_mysql.crypto.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TickerData {
    private String exchange;    // UPBIT, BINANCE
    private String symbol;      // KRW-BTC, BTCUSDT
    private double price;
    private double volume;
    private long timestamp;
} 