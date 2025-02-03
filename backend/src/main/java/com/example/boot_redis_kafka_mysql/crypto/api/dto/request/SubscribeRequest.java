package com.example.boot_redis_kafka_mysql.crypto.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {
    private String exchange;    // UPBIT, BINANCE
    private String market;      // KRW-BTC, BTCUSDT
    private String type;        // ticker, trade
} 