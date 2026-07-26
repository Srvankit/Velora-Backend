package com.veloramarkets.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(

        BigDecimal cashBalance,

        BigDecimal investedValue,
        BigDecimal marketValue,
        BigDecimal totalAccountValue,

        BigDecimal unrealizedPnL,
        BigDecimal realizedPnL,
        BigDecimal totalPnL,
        BigDecimal returnPercentage,

        int totalHoldings,
        int totalOrders,
        int totalTransactions,

        List<DashboardHoldingResponse> topHoldings,
        List<RecentOrderResponse> recentOrders,
        List<RecentTransactionResponse> recentTransactions

) {
}