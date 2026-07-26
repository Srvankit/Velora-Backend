package com.veloramarkets.market.dto;

import java.math.BigDecimal;

public record MarketStockResponse(
        String symbol,
        String companyName,
        BigDecimal price,
        BigDecimal previousClose,
        BigDecimal change,
        BigDecimal changePercent,
        long volume,
        String exchange,
        String sector
) {
}