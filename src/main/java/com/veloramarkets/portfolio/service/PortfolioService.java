package com.veloramarkets.portfolio.service;

import com.veloramarkets.market.service.MarketPriceService;
import com.veloramarkets.portfolio.dto.HoldingResponse;
import com.veloramarkets.portfolio.dto.PortfolioResponse;
import com.veloramarkets.portfolio.entity.Portfolio;
import com.veloramarkets.portfolio.repository.PortfolioRepository;
import com.veloramarkets.trading.entity.OrderSide;
import com.veloramarkets.trading.repository.TradeTransactionRepository;
import com.veloramarkets.user.entity.User;
import com.veloramarkets.user.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioService {

    private static final BigDecimal INITIAL_CASH_BALANCE =
            new BigDecimal("100000.00");

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final MarketPriceService marketPriceService;
    private final TradeTransactionRepository transactionRepository;

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            UserRepository userRepository,
            MarketPriceService marketPriceService,
            TradeTransactionRepository transactionRepository) {

        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.marketPriceService = marketPriceService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public PortfolioResponse getCurrentPortfolio(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );

        Portfolio portfolio = portfolioRepository
                .findByUserId(user.getId())
                .orElseGet(() -> createPortfolio(user));

        return mapToResponse(portfolio);
    }

    private Portfolio createPortfolio(User user) {

        Portfolio portfolio = new Portfolio();

        portfolio.setUser(user);
        portfolio.setCashBalance(INITIAL_CASH_BALANCE);

        return portfolioRepository.save(portfolio);
    }

    private PortfolioResponse mapToResponse(
            Portfolio portfolio) {

        BigDecimal totalInvested =
                BigDecimal.ZERO;

        BigDecimal totalMarketValue =
                BigDecimal.ZERO;

        List<HoldingResponse> holdingResponses =
                new ArrayList<>();

        for (var holding : portfolio.getHoldings()) {

            BigDecimal currentPrice =
                    marketPriceService
                            .getStock(
                                    holding.getSymbol()
                            )
                            .price();

            BigDecimal investedValue =
                    holding.getAverageBuyPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            holding.getQuantity()
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal marketValue =
                    currentPrice
                            .multiply(
                                    BigDecimal.valueOf(
                                            holding.getQuantity()
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal holdingUnrealizedPnL =
                    marketValue
                            .subtract(investedValue)
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
                        holdingUnrealizedPnL
                                .multiply(
                                        new BigDecimal("100")
                                )
                                .divide(
                                        investedValue,
                                        2,
                                        RoundingMode.HALF_UP
                                );
            }

            totalInvested =
                    totalInvested.add(
                            investedValue
                    );

            totalMarketValue =
                    totalMarketValue.add(
                            marketValue
                    );

            holdingResponses.add(
                    new HoldingResponse(
                            holding.getId(),
                            holding.getSymbol(),
                            holding.getCompanyName(),
                            holding.getQuantity(),
                            holding.getAverageBuyPrice(),
                            currentPrice,
                            investedValue,
                            marketValue,
                            holdingUnrealizedPnL,
                            returnPercentage
                    )
            );
        }

        totalInvested =
                totalInvested.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        totalMarketValue =
                totalMarketValue.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal unrealizedPnL =
                totalMarketValue
                        .subtract(totalInvested)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal realizedPnL =
                transactionRepository
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
                realizedPnL
                        .add(unrealizedPnL)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal totalAccountValue =
                portfolio.getCashBalance()
                        .add(totalMarketValue)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getCashBalance(),
                totalInvested,
                totalMarketValue,
                totalAccountValue,
                unrealizedPnL,
                realizedPnL,
                totalPnL,
                holdingResponses.size(),
                holdingResponses,
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }
}