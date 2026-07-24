package com.veloramarkets.portfolio.dto;

import java.math.BigDecimal;

public record HoldingResponse(

        Long id,
        String symbol,
        String companyName,
        long quantity,
        BigDecimal averageBuyPrice,
        BigDecimal currentPrice,
        BigDecimal investedValue,
        BigDecimal marketValue,
        BigDecimal unrealizedPnL,
        BigDecimal returnPercentage

) {
}