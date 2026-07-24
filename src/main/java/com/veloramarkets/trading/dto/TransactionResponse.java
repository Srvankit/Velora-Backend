package com.veloramarkets.trading.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(

        Long transactionId,
        Long orderId,
        String symbol,
        String side,
        long quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        BigDecimal realizedPnL,
        LocalDateTime executedAt

) {
}