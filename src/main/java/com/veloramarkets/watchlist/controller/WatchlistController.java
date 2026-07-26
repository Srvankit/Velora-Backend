package com.veloramarkets.watchlist.controller;

import com.veloramarkets.watchlist.dto.WatchlistResponse;
import com.veloramarkets.watchlist.service.WatchlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(
            WatchlistService watchlistService
    ) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public ResponseEntity<List<WatchlistResponse>>
    getWatchlist(Authentication authentication) {

        return ResponseEntity.ok(
                watchlistService.getWatchlist(authentication)
        );
    }

    @PostMapping("/{symbol}")
    public ResponseEntity<WatchlistResponse> addStock(
            Authentication authentication,
            @PathVariable String symbol
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        watchlistService.addStock(
                                authentication,
                                symbol
                        )
                );
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<Void> removeStock(
            Authentication authentication,
            @PathVariable String symbol
    ) {
        watchlistService.removeStock(
                authentication,
                symbol
        );

        return ResponseEntity.noContent().build();
    }
}