package com.veloramarkets.trading.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderHistoryResponse(

        Long orderId,
        String symbol,
        String companyName,
        String side,
        String orderType,
        String status,
        long quantity,
        BigDecimal limitPrice,
        BigDecimal executionPrice,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime executedAt

) {
}