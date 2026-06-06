package com.ledger.valuation.interfaces.messaging;

import com.ledger.valuation.application.port.inbound.ProcessCommandUseCase;
import com.ledger.valuation.domain.Command;
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
    private final IngressCommandMapper commandMapper;
    private final ProcessCommandUseCase processCommandUseCase;

    public NormalizedMarketTickConsumer(
            OpenTelemetryKafkaConsumerTracing consumerTracing,
            IngressCommandMapper commandMapper,
            ProcessCommandUseCase processCommandUseCase
    ) {
        this.consumerTracing = consumerTracing;
        this.commandMapper = commandMapper;
        this.processCommandUseCase = processCommandUseCase;
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
        Command command = commandMapper.toCommand(record.value());
        log.debug(
                "NormalizedMarketTick received topic={} partition={} offset={} type={}",
                record.topic(),
                record.partition(),
                record.offset(),
                command.getClass().getSimpleName()
        );
        processCommandUseCase.process(command);
    }
}
