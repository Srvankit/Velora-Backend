package com.veloramarkets.dashboard.dto;

import com.veloramarkets.trading.entity.OrderSide;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentTransactionResponse(
        Long id,
        String symbol,
        OrderSide side,
        long quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        BigDecimal realizedPnL,
        LocalDateTime executedAt
) {
}