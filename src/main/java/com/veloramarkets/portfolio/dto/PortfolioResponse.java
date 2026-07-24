package com.veloramarkets.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PortfolioResponse(

        Long portfolioId,
        BigDecimal cashBalance,
        BigDecimal investedValue,
        BigDecimal marketValue,
        BigDecimal totalAccountValue,
        BigDecimal unrealizedPnL,
        BigDecimal realizedPnL,
        BigDecimal totalPnL,
        int totalHoldings,
        List<HoldingResponse> holdings,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}