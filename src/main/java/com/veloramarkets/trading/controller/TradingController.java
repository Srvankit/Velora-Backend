package com.veloramarkets.trading.controller;

import com.veloramarkets.trading.dto.OrderResponse;
import com.veloramarkets.trading.dto.PlaceOrderRequest;
import com.veloramarkets.trading.service.TradingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.veloramarkets.trading.dto.OrderHistoryResponse;
import com.veloramarkets.trading.dto.TransactionResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderHistoryResponse>>
    getOrderHistory(
            Authentication authentication,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size) {

        int safeSize = Math.min(
                Math.max(size, 1),
                100
        );

        Pageable pageable =
                PageRequest.of(
                        Math.max(page, 0),
                        safeSize,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return ResponseEntity.ok(
                tradingService.getOrderHistory(
                        authentication,
                        pageable
                )
        );
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>>
    getTransactionHistory(
            Authentication authentication,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size) {

        int safeSize = Math.min(
                Math.max(size, 1),
                100
        );

        Pageable pageable =
                PageRequest.of(
                        Math.max(page, 0),
                        safeSize,
                        Sort.by(
                                Sort.Direction.DESC,
                                "executedAt"
                        )
                );

        return ResponseEntity.ok(
                tradingService.getTransactionHistory(
                        authentication,
                        pageable
                )
        );
    }
}