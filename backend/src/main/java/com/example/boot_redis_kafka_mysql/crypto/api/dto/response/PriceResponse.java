package com.example.boot_redis_kafka_mysql.crypto.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponse {
    private String exchange;
    private String market;
    private double price;
    private double volume;
    private long timestamp;
} 