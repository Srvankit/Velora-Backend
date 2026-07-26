package com.veloramarkets.watchlist.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WatchlistResponse(
        Long id,
        String symbol,
        String companyName,
        BigDecimal currentPrice,
        LocalDateTime addedAt
) {
}