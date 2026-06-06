package com.ledger.valuation.interfaces.messaging;

import com.ledger.valuation.application.service.MarketTickValuationService;
import com.ledger.valuation.domain.NormalizedMarketTick;
import com.ledger.valuation.infrastructure.telemetry.OpenTelemetryKafkaConsumerTracing;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NormalizedMarketTickConsumer {

    private static final Logger log = LoggerFactory.getLogger(NormalizedMarketTickConsumer.class);
    private static final String CONSUMER_SPAN_NAME = "NormalizedMarketTicks consume";

    private final OpenTelemetryKafkaConsumerTracing consumerTracing;
    private final NormalizedMarketTickMapper tickMapper;
    private final MarketTickValuationService valuationService;

    public NormalizedMarketTickConsumer(
            OpenTelemetryKafkaConsumerTracing consumerTracing,
            NormalizedMarketTickMapper tickMapper,
            MarketTickValuationService valuationService
    ) {
        this.consumerTracing = consumerTracing;
        this.tickMapper = tickMapper;
        this.valuationService = valuationService;
    }

    @KafkaListener(
            topics = "${ledger.ingress.market-ticks-topic}",
            containerFactory = "marketTickKafkaListenerContainerFactory",
            groupId = "${ledger.ingress.market-ticks-group-id}"
    )
    public void onNormalizedMarketTick(ConsumerRecord<String, String> record) {
        consumerTracing.consume(record, CONSUMER_SPAN_NAME, () -> processRecord(record));
    }

    private void processRecord(ConsumerRecord<String, String> record) {
        NormalizedMarketTick tick = tickMapper.toTick(record.value());
        log.debug(
                "NormalizedMarketTick instrument={} price={} runId={}",
                tick.instrumentId(),
                tick.priceMinorUnits(),
                tick.valuationRunId()
        );
        valuationService.processTick(tick);
    }
}
