package com.example.boot_redis_kafka_mysql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BootRedisKafkaMysqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootRedisKafkaMysqlApplication.class, args);
	}

}
