package com.veloramarkets.dashboard.service;

import com.veloramarkets.dashboard.dto.*;
import com.veloramarkets.market.service.MarketPriceService;
import com.veloramarkets.portfolio.entity.Portfolio;
import com.veloramarkets.portfolio.repository.PortfolioRepository;
import com.veloramarkets.trading.entity.OrderSide;
import com.veloramarkets.trading.repository.TradeOrderRepository;
import com.veloramarkets.trading.repository.TradeTransactionRepository;
import com.veloramarkets.user.entity.User;
import com.veloramarkets.user.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final MarketPriceService marketPriceService;
    private final TradeOrderRepository tradeOrderRepository;
    private final TradeTransactionRepository tradeTransactionRepository;

    public DashboardService(
            UserRepository userRepository,
            PortfolioRepository portfolioRepository,
            MarketPriceService marketPriceService,
            TradeOrderRepository tradeOrderRepository,
            TradeTransactionRepository tradeTransactionRepository
    ) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.marketPriceService = marketPriceService;
        this.tradeOrderRepository = tradeOrderRepository;
        this.tradeTransactionRepository = tradeTransactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(
            Authentication authentication
    ) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );

        Portfolio portfolio = portfolioRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Portfolio not found"
                        )
                );

        BigDecimal investedValue = BigDecimal.ZERO;
        BigDecimal marketValue = BigDecimal.ZERO;

        for (var holding : portfolio.getHoldings()) {

            BigDecimal quantity =
                    BigDecimal.valueOf(
                            holding.getQuantity()
                    );

            BigDecimal invested =
                    holding.getAverageBuyPrice()
                            .multiply(quantity);

            BigDecimal currentPrice =
                    marketPriceService
                            .getStock(
                                    holding.getSymbol()
                            )
                            .price();

            BigDecimal currentValue =
                    currentPrice.multiply(quantity);

            investedValue =
                    investedValue.add(invested);

            marketValue =
                    marketValue.add(currentValue);
        }

        investedValue =
                investedValue.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        marketValue =
                marketValue.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal unrealizedPnL =
                marketValue
                        .subtract(investedValue)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal realizedPnL =
                tradeTransactionRepository
                        .sumRealizedPnLByPortfolioIdAndSide(
                                portfolio.getId(),
                                OrderSide.SELL
                        );

        if (realizedPnL == null) {
            realizedPnL = BigDecimal.ZERO;
        }

        realizedPnL =
                realizedPnL.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal totalPnL =
                unrealizedPnL
                        .add(realizedPnL)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal totalAccountValue =
                portfolio.getCashBalance()
                        .add(marketValue)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal returnPercentage;

        if (investedValue.compareTo(
                BigDecimal.ZERO) == 0) {

            returnPercentage =
                    BigDecimal.ZERO.setScale(2);

        } else {

            returnPercentage =
                    totalPnL
                            .multiply(
                                    new BigDecimal("100")
                            )
                            .divide(
                                    investedValue,
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        List<DashboardHoldingResponse> topHoldings =
                portfolio.getHoldings()
                        .stream()
                        .map(holding -> {

                            BigDecimal quantity =
                                    BigDecimal.valueOf(
                                            holding.getQuantity()
                                    );

                            BigDecimal invested =
                                    holding.getAverageBuyPrice()
                                            .multiply(quantity);

                            BigDecimal currentPrice =
                                    marketPriceService
                                            .getStock(
                                                    holding.getSymbol()
                                            )
                                            .price();

                            BigDecimal currentValue =
                                    currentPrice
                                            .multiply(quantity)
                                            .setScale(
                                                    2,
                                                    RoundingMode.HALF_UP
                                            );

                            BigDecimal pnl =
                                    currentValue
                                            .subtract(invested)
                                            .setScale(
                                                    2,
                                                    RoundingMode.HALF_UP
                                            );

                            BigDecimal holdingReturn;

                            if (invested.compareTo(
                                    BigDecimal.ZERO) == 0) {

                                holdingReturn =
                                        BigDecimal.ZERO
                                                .setScale(2);

                            } else {

                                holdingReturn =
                                        pnl.multiply(
                                                new BigDecimal("100")
                                        ).divide(
                                                invested,
                                                2,
                                                RoundingMode.HALF_UP
                                        );
                            }

                            return new DashboardHoldingResponse(
                                    holding.getSymbol(),
                                    holding.getCompanyName(),
                                    holding.getQuantity(),
                                    holding.getAverageBuyPrice(),
                                    currentPrice,
                                    currentValue,
                                    pnl,
                                    holdingReturn
                            );
                        })
                        .sorted(
                                Comparator.comparing(
                                        DashboardHoldingResponse::marketValue
                                ).reversed()
                        )
                        .limit(5)
                        .toList();

        List<RecentOrderResponse> recentOrders =
                tradeOrderRepository
                        .findTop5ByPortfolioIdOrderByCreatedAtDesc(
                                portfolio.getId()
                        )
                        .stream()
                        .map(order ->
                                new RecentOrderResponse(
                                        order.getId(),
                                        order.getSymbol(),
                                        order.getCompanyName(),
                                        order.getSide(),
                                        order.getOrderType(),
                                        order.getStatus(),
                                        order.getQuantity(),
                                        order.getExecutionPrice(),
                                        order.getTotalAmount(),
                                        order.getCreatedAt(),
                                        order.getExecutedAt()
                                )
                        )
                        .toList();

        List<RecentTransactionResponse>
                recentTransactions =
                tradeTransactionRepository
                        .findTop5ByPortfolioIdOrderByExecutedAtDesc(
                                portfolio.getId()
                        )
                        .stream()
                        .map(transaction ->
                                new RecentTransactionResponse(
                                        transaction.getId(),
                                        transaction.getSymbol(),
                                        transaction.getSide(),
                                        transaction.getQuantity(),
                                        transaction.getPrice(),
                                        transaction.getTotalAmount(),
                                        transaction.getRealizedPnL(),
                                        transaction.getExecutedAt()
                                )
                        )
                        .toList();

        long totalOrders =
                tradeOrderRepository.countByPortfolioId(
                        portfolio.getId()
                );

        long totalTransactions =
                tradeTransactionRepository
                        .countByPortfolioId(
                                portfolio.getId()
                        );

        return new DashboardResponse(
                portfolio.getCashBalance(),
                investedValue,
                marketValue,
                totalAccountValue,
                unrealizedPnL,
                realizedPnL,
                totalPnL,
                returnPercentage,
                portfolio.getHoldings().size(),
                Math.toIntExact(totalOrders),
                Math.toIntExact(totalTransactions),
                topHoldings,
                recentOrders,
                recentTransactions
        );
    }
}