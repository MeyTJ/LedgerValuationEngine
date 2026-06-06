package com.ledger.valuation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class LedgerValuationEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerValuationEngineApplication.class, args);
    }
}
