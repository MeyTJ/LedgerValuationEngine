package com.ledger.valuation.infrastructure.config;

import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class VirtualThreadTomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatVirtualThreadCustomizer(
            @Value("${ledger.tomcat.max-connections:10000}") int maxConnections
    ) {
        ThreadFactory threadFactory = Thread.ofVirtual()
                .name("lve-tomcat-", 0)
                .factory();

        return factory -> factory.addConnectorCustomizers(connector -> {
            ProtocolHandler handler = connector.getProtocolHandler();
            handler.setMaxConnections(maxConnections);
            if (handler instanceof AbstractProtocol<?> protocol) {
                protocol.setExecutor(Executors.newThreadPerTaskExecutor(threadFactory));
            }
        });
    }
}
