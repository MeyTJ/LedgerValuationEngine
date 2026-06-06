package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.application.port.inbound.RebuildPortfolioReadSideUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final RebuildPortfolioReadSideUseCase rebuildUseCase;

    public AdminController(RebuildPortfolioReadSideUseCase rebuildUseCase) {
        this.rebuildUseCase = rebuildUseCase;
    }

    @PostMapping("/readside/rebuild")
    public ResponseEntity<Void> rebuildAll() {
        rebuildUseCase.rebuildAll();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/readside/rebuild/{portfolioId}")
    public ResponseEntity<Void> rebuildPortfolio(@PathVariable UUID portfolioId) {
        rebuildUseCase.rebuildPortfolio(portfolioId);
        return ResponseEntity.accepted().build();
    }
}
