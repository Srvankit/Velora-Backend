package com.veloramarkets.dashboard.dto;

import com.veloramarkets.trading.entity.OrderSide;
import com.veloramarkets.trading.entity.OrderStatus;
import com.veloramarkets.trading.entity.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentOrderResponse(
        Long id,
        String symbol,
        String companyName,
        OrderSide side,
        OrderType orderType,
        OrderStatus status,
        long quantity,
        BigDecimal executionPrice,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime executedAt
) {
}