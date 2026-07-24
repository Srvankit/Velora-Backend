package com.veloramarkets.portfolio.dto;

import java.math.BigDecimal;

public record HoldingResponse(

        Long id,
        String symbol,
        String companyName,
        long quantity,
        BigDecimal averageBuyPrice

) {
}