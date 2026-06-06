package com.ledger.valuation.interfaces.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.domain.Command;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IngressCommandMapper {

    private final ObjectMapper objectMapper;

    public IngressCommandMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Command toCommand(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String type = root.path("type").asText();
            UUID correlationId = UUID.fromString(root.path("correlationId").asText());

            return switch (type) {
                case "OpenAccount" -> new Command.OpenAccount(
                        correlationId,
                        root.path("accountCode").asText(),
                        root.path("currency").asText()
                );
                case "PostTransaction" -> new Command.PostTransaction(
                        correlationId,
                        root.path("debitAccount").asText(),
                        root.path("creditAccount").asText(),
                        root.path("amountMinorUnits").asLong()
                );
                case "ValueAccount" -> new Command.ValueAccount(
                        correlationId,
                        root.path("accountCode").asText()
                );
                default -> throw new IllegalArgumentException("Unknown command type: " + type);
            };
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid ingress payload", ex);
        }
    }
}
