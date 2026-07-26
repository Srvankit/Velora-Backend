package com.veloramarkets.market.controller;

import com.veloramarkets.market.dto.MarketStockResponse;
import com.veloramarkets.market.service.MarketPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    private final MarketPriceService marketPriceService;

    public MarketController(
            MarketPriceService marketPriceService
    ) {
        this.marketPriceService = marketPriceService;
    }

    @GetMapping("/stocks")
    public ResponseEntity<List<MarketStockResponse>>
    getAllStocks() {

        return ResponseEntity.ok(
                marketPriceService.getAllStocks()
                        .stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<MarketStockResponse>
    getStock(
            @PathVariable String symbol
    ) {

        String normalizedSymbol =
                symbol.trim().toUpperCase();

        MarketPriceService.MarketStock stock =
                marketPriceService.getStock(
                        normalizedSymbol
                );

        return ResponseEntity.ok(
                new MarketStockResponse(
                        normalizedSymbol,
                        stock.companyName(),
                        stock.price(),
                        stock.previousClose(),
                        stock.change(),
                        stock.changePercent(),
                        stock.volume(),
                        stock.exchange(),
                        stock.sector()
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<MarketStockResponse>>
    searchStocks(
            @RequestParam String query
    ) {

        return ResponseEntity.ok(
                marketPriceService.searchStocks(query)
                        .stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    @GetMapping("/gainers")
    public ResponseEntity<List<MarketStockResponse>>
    getTopGainers() {

        return ResponseEntity.ok(
                marketPriceService.getTopGainers()
                        .stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    @GetMapping("/losers")
    public ResponseEntity<List<MarketStockResponse>>
    getTopLosers() {

        return ResponseEntity.ok(
                marketPriceService.getTopLosers()
                        .stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<MarketStockResponse>>
    getMostActive() {

        return ResponseEntity.ok(
                marketPriceService.getMostActive()
                        .stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    private MarketStockResponse mapToResponse(
            MarketPriceService.MarketStockEntry stock
    ) {

        return new MarketStockResponse(
                stock.symbol(),
                stock.companyName(),
                stock.price(),
                stock.previousClose(),
                stock.change(),
                stock.changePercent(),
                stock.volume(),
                stock.exchange(),
                stock.sector()
        );
    }
}