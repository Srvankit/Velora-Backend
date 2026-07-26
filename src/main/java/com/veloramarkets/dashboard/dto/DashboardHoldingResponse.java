package com.veloramarkets.dashboard.dto;

import java.math.BigDecimal;

public record DashboardHoldingResponse(
        String symbol,
        String companyName,
        long quantity,
        BigDecimal averageBuyPrice,
        BigDecimal currentPrice,
        BigDecimal marketValue,
        BigDecimal unrealizedPnL,
        BigDecimal returnPercentage
) {
}