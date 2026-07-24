package com.veloramarkets.trading.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(

        Long orderId,
        String symbol,
        String companyName,
        String side,
        String orderType,
        String status,
        long quantity,
        BigDecimal executionPrice,
        BigDecimal totalAmount,
        BigDecimal remainingCashBalance,
        LocalDateTime executedAt,
        String message

) {
}