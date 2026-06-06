package com.ledger.valuation.interfaces.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.domain.NormalizedMarketTick;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class NormalizedMarketTickMapper {

    private final ObjectMapper objectMapper;

    public NormalizedMarketTickMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NormalizedMarketTick toTick(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            return new NormalizedMarketTick(
                    root.path("instrumentId").asText(),
                    root.path("quoteCurrency").asText("USD"),
                    root.path("priceMinorUnits").asLong(),
                    Instant.parse(root.path("tickTimestamp").asText()),
                    root.path("valuationRunId").asText()
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid NormalizedMarketTick payload", ex);
        }
    }
}
