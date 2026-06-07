package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.application.port.outbound.AccountValueStreamPort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/stream")
public class AccountValueStreamController {

    private final AccountValueStreamPort streamPort;

    public AccountValueStreamController(AccountValueStreamPort streamPort) {
        this.streamPort = streamPort;
    }

    @GetMapping(value = "/account-values", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAccountValues(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId
    ) {
        AccountValueStreamPort.StreamSubscription subscription =
                streamPort.subscribe(tenantId == null ? "*" : tenantId);
        return (SseEmitter) subscription.emitterHandle();
    }
}
