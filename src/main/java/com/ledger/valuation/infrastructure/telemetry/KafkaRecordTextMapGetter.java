package com.ledger.valuation.infrastructure.telemetry;

import io.opentelemetry.context.propagation.TextMapGetter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

public enum KafkaRecordTextMapGetter implements TextMapGetter<ConsumerRecord<?, ?>> {

    INSTANCE;

    @Override
    public Iterable<String> keys(ConsumerRecord<?, ?> carrier) {
        Set<String> keys = new LinkedHashSet<>();
        for (Header header : carrier.headers()) {
            keys.add(header.key());
        }
        return keys;
    }

    @Override
    public String get(ConsumerRecord<?, ?> carrier, String key) {
        Header header = carrier.headers().lastHeader(key);
        if (header == null || header.value() == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
