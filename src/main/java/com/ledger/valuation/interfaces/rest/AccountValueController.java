package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.application.port.inbound.QueryAccountValueUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountValueController {

    private final QueryAccountValueUseCase queryAccountValueUseCase;

    public AccountValueController(QueryAccountValueUseCase queryAccountValueUseCase) {
        this.queryAccountValueUseCase = queryAccountValueUseCase;
    }

    @GetMapping("/{accountCode}/value")
    public ResponseEntity<AccountValueResponse> getValue(@PathVariable String accountCode) {
        Long value = queryAccountValueUseCase.getAccountValue(accountCode);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new AccountValueResponse(accountCode, value));
    }

    public record AccountValueResponse(String accountCode, long accountValueMinorUnits) {}
}
