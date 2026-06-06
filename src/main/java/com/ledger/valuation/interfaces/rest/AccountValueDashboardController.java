package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.application.port.inbound.QueryAccountValueDashboardUseCase;
import com.ledger.valuation.application.readmodel.AccountValueDashboardView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
public class AccountValueDashboardController {

    private final QueryAccountValueDashboardUseCase dashboardQueryUseCase;

    public AccountValueDashboardController(QueryAccountValueDashboardUseCase dashboardQueryUseCase) {
        this.dashboardQueryUseCase = dashboardQueryUseCase;
    }

    @GetMapping("/account-values")
    public List<AccountValueDashboardResponse> getDashboardSnapshot() {
        return dashboardQueryUseCase.getDashboardSnapshot().stream()
                .map(AccountValueDashboardResponse::from)
                .toList();
    }

    @GetMapping("/account-values/{accountCode}")
    public ResponseEntity<AccountValueDashboardResponse> getByAccountCode(@PathVariable String accountCode) {
        return dashboardQueryUseCase.getByAccountCode(accountCode)
                .map(AccountValueDashboardResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/portfolios/{portfolioId}/account-value")
    public ResponseEntity<AccountValueDashboardResponse> getByPortfolioId(@PathVariable UUID portfolioId) {
        return dashboardQueryUseCase.getByPortfolioId(portfolioId)
                .map(AccountValueDashboardResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record AccountValueDashboardResponse(
            UUID portfolioId,
            String accountCode,
            String tenantId,
            String currency,
            long accountValueMinorUnits,
            long lastSequenceNumber,
            Instant lastUpdatedAt
    ) {

        static AccountValueDashboardResponse from(AccountValueDashboardView view) {
            return new AccountValueDashboardResponse(
                    view.portfolioId(),
                    view.accountCode(),
                    view.tenantId(),
                    view.currency(),
                    view.accountValueMinorUnits(),
                    view.lastSequenceNumber(),
                    view.lastUpdatedAt()
            );
        }
    }
}
