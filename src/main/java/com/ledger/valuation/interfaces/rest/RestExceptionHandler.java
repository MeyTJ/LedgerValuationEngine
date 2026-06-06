package com.ledger.valuation.interfaces.rest;

import com.ledger.valuation.domain.InsufficientFundsException;
import com.ledger.valuation.domain.PolicyViolationException;
import com.ledger.valuation.domain.PortfolioNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(PortfolioNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(PortfolioNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientFunds(InsufficientFundsException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(PolicyViolationException.class)
    public ResponseEntity<Map<String, String>> handlePolicyViolation(PolicyViolationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", ex.getMessage(),
                "ruleType", ex.ruleType().name()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
}
