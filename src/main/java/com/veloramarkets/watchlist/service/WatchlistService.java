package com.veloramarkets.watchlist.service;

import com.veloramarkets.common.exception.BadRequestException;
import com.veloramarkets.market.service.MarketPriceService;
import com.veloramarkets.user.entity.User;
import com.veloramarkets.user.repository.UserRepository;
import com.veloramarkets.watchlist.dto.WatchlistResponse;
import com.veloramarkets.watchlist.entity.WatchlistItem;
import com.veloramarkets.watchlist.repository.WatchlistItemRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WatchlistService {

    private final WatchlistItemRepository watchlistItemRepository;
    private final UserRepository userRepository;
    private final MarketPriceService marketPriceService;

    public WatchlistService(
            WatchlistItemRepository watchlistItemRepository,
            UserRepository userRepository,
            MarketPriceService marketPriceService
    ) {
        this.watchlistItemRepository = watchlistItemRepository;
        this.userRepository = userRepository;
        this.marketPriceService = marketPriceService;
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> getWatchlist(
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        return watchlistItemRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public WatchlistResponse addStock(
            Authentication authentication,
            String symbol
    ) {
        User user = getAuthenticatedUser(authentication);

        String normalizedSymbol = normalizeSymbol(symbol);

        // Validates that the stock is supported.
        marketPriceService.getStock(normalizedSymbol);

        if (watchlistItemRepository.existsByUserIdAndSymbol(
                user.getId(),
                normalizedSymbol
        )) {
            throw new BadRequestException(
                    "Stock is already in watchlist"
            );
        }

        WatchlistItem item = new WatchlistItem();

        item.setUser(user);
        item.setSymbol(normalizedSymbol);

        WatchlistItem saved =
                watchlistItemRepository.save(item);

        return mapToResponse(saved);
    }

    @Transactional
    public void removeStock(
            Authentication authentication,
            String symbol
    ) {
        User user = getAuthenticatedUser(authentication);

        String normalizedSymbol = normalizeSymbol(symbol);

        WatchlistItem item = watchlistItemRepository
                .findByUserIdAndSymbol(
                        user.getId(),
                        normalizedSymbol
                )
                .orElseThrow(() ->
                        new BadRequestException(
                                "Stock is not in watchlist"
                        )
                );

        watchlistItemRepository.delete(item);
    }

    private WatchlistResponse mapToResponse(
            WatchlistItem item
    ) {
        MarketPriceService.MarketStock stock =
                marketPriceService.getStock(
                        item.getSymbol()
                );

        return new WatchlistResponse(
                item.getId(),
                item.getSymbol(),
                stock.companyName(),
                stock.price(),
                item.getCreatedAt()
        );
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {
        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );
    }

    private String normalizeSymbol(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            throw new BadRequestException(
                    "Stock symbol is required"
            );
        }

        return symbol.trim().toUpperCase();
    }
}