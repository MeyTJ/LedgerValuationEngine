package com.ledger.valuation.interfaces.messaging;

import com.ledger.valuation.application.port.inbound.ProcessCommandUseCase;
import com.ledger.valuation.domain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SignalIngressBroker {

    private static final Logger log = LoggerFactory.getLogger(SignalIngressBroker.class);

    private final ProcessCommandUseCase processCommandUseCase;
    private final IngressCommandMapper mapper;

    public SignalIngressBroker(ProcessCommandUseCase processCommandUseCase, IngressCommandMapper mapper) {
        this.processCommandUseCase = processCommandUseCase;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "${ledger.ingress.topic}")
    public void onSignal(String payload) {
        Command command = mapper.toCommand(payload);
        log.debug("Ingress signal received type={}", command.getClass().getSimpleName());
        processCommandUseCase.process(command);
    }
}
