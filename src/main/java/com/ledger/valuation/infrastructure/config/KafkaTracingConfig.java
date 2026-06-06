package com.ledger.valuation.infrastructure.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;

@Configuration
public class KafkaTracingConfig {

    @Bean
    public RecordInterceptor<String, String> w3cRecordInterceptor(Tracer tracer, Propagator propagator) {
        return new RecordInterceptor<>() {
            @Override
            public ConsumerRecord<String, String> intercept(ConsumerRecord<String, String> record) {
                Span span = propagator.extract(record, KafkaTracingConfig::headerValue)
                        .name("kafka.consume")
                        .tag("messaging.system", "kafka")
                        .tag("messaging.destination", record.topic())
                        .start();
                tracer.withSpan(span);
                return record;
            }

            @Override
            public void afterRecord(ConsumerRecord<String, String> record, Exception exception) {
                Span span = tracer.currentSpan();
                if (span != null) {
                    span.end();
                }
            }
        };
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            RecordInterceptor<String, String> w3cRecordInterceptor
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.setRecordInterceptor(w3cRecordInterceptor);
        return factory;
    }

    private static String headerValue(ConsumerRecord<String, String> record, String key) {
        Header header = record.headers().lastHeader(key);
        if (header == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
