package com.ledger.valuation.infrastructure.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
public class OpenTelemetryKafkaConsumerTracing {

    private static final String INSTRUMENTATION_SCOPE = "com.ledger.valuation.kafka";

    private final Tracer tracer;
    private final TextMapPropagator propagator;
    private final String consumerGroupId;

    public OpenTelemetryKafkaConsumerTracing(
            OpenTelemetry openTelemetry,
            TextMapPropagator textMapPropagator,
            @org.springframework.beans.factory.annotation.Value("${ledger.ingress.market-ticks-group-id}") String consumerGroupId
    ) {
        this.tracer = openTelemetry.getTracer(INSTRUMENTATION_SCOPE);
        this.propagator = textMapPropagator;
        this.consumerGroupId = consumerGroupId;
    }

    public void consume(ConsumerRecord<String, String> record, String spanName, Runnable processing) {
        Context extractedContext = propagator.extract(Context.current(), record, KafkaRecordTextMapGetter.INSTANCE);

        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.CONSUMER)
                .setParent(extractedContext)
                .setAttribute("messaging.system", "kafka")
                .setAttribute("messaging.operation", "receive")
                .setAttribute("messaging.destination.name", record.topic())
                .setAttribute("messaging.kafka.partition", record.partition())
                .setAttribute("messaging.kafka.offset", record.offset())
                .startSpan();

        String traceparent = KafkaRecordTextMapGetter.INSTANCE.get(record, "traceparent");
        if (traceparent != null) {
            span.setAttribute("w3c.traceparent", traceparent);
        }

        try (Scope scope = span.makeCurrent()) {
            processing.run();
        } catch (RuntimeException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, ex.getMessage());
            throw ex;
        } finally {
            span.end();
        }
    }
}
