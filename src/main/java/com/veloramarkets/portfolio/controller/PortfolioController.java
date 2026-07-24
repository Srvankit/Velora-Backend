package com.veloramarkets.portfolio.controller;

import com.veloramarkets.portfolio.dto.PortfolioResponse;
import com.veloramarkets.portfolio.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(
            PortfolioService portfolioService) {

        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<PortfolioResponse> getPortfolio(
            Authentication authentication) {

        return ResponseEntity.ok(
                portfolioService.getCurrentPortfolio(authentication)
        );
    }
}