package com.veloramarkets.trading.controller;

import com.veloramarkets.trading.dto.OrderResponse;
import com.veloramarkets.trading.dto.PlaceOrderRequest;
import com.veloramarkets.trading.service.TradingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trading")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(
            TradingService tradingService) {

        this.tradingService = tradingService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            Authentication authentication) {

        OrderResponse response =
                tradingService.placeOrder(
                        request,
                        authentication
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}