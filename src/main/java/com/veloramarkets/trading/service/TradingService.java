package com.veloramarkets.trading.service;

import com.veloramarkets.common.exception.BadRequestException;
import com.veloramarkets.market.service.MarketPriceService;
import com.veloramarkets.portfolio.entity.Holding;
import com.veloramarkets.portfolio.entity.Portfolio;
import com.veloramarkets.portfolio.repository.HoldingRepository;
import com.veloramarkets.portfolio.repository.PortfolioRepository;
import com.veloramarkets.trading.dto.OrderResponse;
import com.veloramarkets.trading.dto.PlaceOrderRequest;
import com.veloramarkets.trading.entity.*;
import com.veloramarkets.trading.repository.TradeOrderRepository;
import com.veloramarkets.trading.repository.TradeTransactionRepository;
import com.veloramarkets.user.entity.User;
import com.veloramarkets.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.veloramarkets.trading.dto.OrderHistoryResponse;
import com.veloramarkets.trading.dto.TransactionResponse;

import java.util.List;

@Service
public class TradingService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final TradeOrderRepository orderRepository;
    private final TradeTransactionRepository transactionRepository;
    private final MarketPriceService marketPriceService;

    public TradingService(
            UserRepository userRepository,
            PortfolioRepository portfolioRepository,
            HoldingRepository holdingRepository,
            TradeOrderRepository orderRepository,
            TradeTransactionRepository transactionRepository,
            MarketPriceService marketPriceService) {

        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.marketPriceService = marketPriceService;
    }

    @Transactional
    public OrderResponse placeOrder(
            PlaceOrderRequest request,
            Authentication authentication) {

        if (request.orderType() != OrderType.MARKET) {
            throw new BadRequestException(
                    "Only MARKET orders are supported currently"
            );
        }

        User user = getAuthenticatedUser(authentication);

        Portfolio portfolio = portfolioRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Portfolio not found"
                        )
                );

        String symbol =
                request.symbol()
                        .trim()
                        .toUpperCase();

        MarketPriceService.MarketStock stock =
                marketPriceService.getStock(symbol);

        if (request.side() == OrderSide.BUY) {

            return executeBuy(
                    portfolio,
                    symbol,
                    stock,
                    request.quantity()
            );
        }

        if (request.side() == OrderSide.SELL) {

            return executeSell(
                    portfolio,
                    symbol,
                    stock,
                    request.quantity()
            );
        }

        throw new BadRequestException(
                "Unsupported order side"
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderHistoryResponse> getOrderHistory(
            Authentication authentication,
            Pageable pageable) {

        User user = getAuthenticatedUser(authentication);

        Portfolio portfolio = portfolioRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Portfolio not found"
                        )
                );

        return orderRepository
                .findAllByPortfolioId(
                        portfolio.getId(),
                        pageable
                )
                .map(order ->
                        new OrderHistoryResponse(
                                order.getId(),
                                order.getSymbol(),
                                order.getCompanyName(),
                                order.getSide().name(),
                                order.getOrderType().name(),
                                order.getStatus().name(),
                                order.getQuantity(),
                                order.getLimitPrice(),
                                order.getExecutionPrice(),
                                order.getTotalAmount(),
                                order.getCreatedAt(),
                                order.getExecutedAt()
                        )
                );
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(
            Authentication authentication,
            Pageable pageable) {

        User user = getAuthenticatedUser(authentication);

        Portfolio portfolio = portfolioRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Portfolio not found"
                        )
                );

        return transactionRepository
                .findAllByPortfolioId(
                        portfolio.getId(),
                        pageable
                )
                .map(transaction ->
                        new TransactionResponse(
                                transaction.getId(),
                                transaction.getOrder().getId(),
                                transaction.getSymbol(),
                                transaction.getSide().name(),
                                transaction.getQuantity(),
                                transaction.getPrice(),
                                transaction.getTotalAmount(),
                                transaction.getRealizedPnL(),
                                transaction.getExecutedAt()
                        )
                );
    }

    private OrderResponse executeBuy(
            Portfolio portfolio,
            String symbol,
            MarketPriceService.MarketStock stock,
            long quantity) {

        BigDecimal executionPrice = stock.price();

        BigDecimal totalAmount =
                calculateTotal(
                        executionPrice,
                        quantity
                );

        if (portfolio.getCashBalance()
                .compareTo(totalAmount) < 0) {

            throw new BadRequestException(
                    "Insufficient cash balance"
            );
        }

        TradeOrder order = createPendingOrder(
                portfolio,
                symbol,
                stock.companyName(),
                OrderSide.BUY,
                quantity
        );

        updateHoldingForBuy(
                portfolio,
                symbol,
                stock.companyName(),
                quantity,
                executionPrice
        );

        BigDecimal remainingBalance =
                portfolio.getCashBalance()
                        .subtract(totalAmount)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        portfolio.setCashBalance(
                remainingBalance
        );

        portfolioRepository.save(portfolio);

        LocalDateTime executedAt =
                LocalDateTime.now();

        completeOrder(
                order,
                executionPrice,
                totalAmount,
                executedAt
        );

        createTransaction(
                portfolio,
                order,
                symbol,
                OrderSide.BUY,
                quantity,
                executionPrice,
                totalAmount,
                null,
                executedAt
        );

        return new OrderResponse(
                order.getId(),
                symbol,
                stock.companyName(),
                OrderSide.BUY.name(),
                OrderType.MARKET.name(),
                OrderStatus.EXECUTED.name(),
                quantity,
                executionPrice,
                totalAmount,
                remainingBalance,
                executedAt,
                "Buy order executed successfully"
        );
    }

    private OrderResponse executeSell(
            Portfolio portfolio,
            String symbol,
            MarketPriceService.MarketStock stock,
            long quantity) {

        Holding holding = holdingRepository
                .findByPortfolioIdAndSymbol(
                        portfolio.getId(),
                        symbol
                )
                .orElseThrow(() ->
                        new BadRequestException(
                                "You do not own this stock"
                        )
                );

        if (holding.getQuantity() < quantity) {

            throw new BadRequestException(
                    "Insufficient shares to sell"
            );
        }

        BigDecimal executionPrice =
                stock.price();

        BigDecimal totalAmount =
                calculateTotal(
                        executionPrice,
                        quantity
                );

        BigDecimal costBasis =
                holding.getAverageBuyPrice()
                        .multiply(
                                BigDecimal.valueOf(quantity)
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal realizedPnL =
                totalAmount
                        .subtract(costBasis)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        TradeOrder order = createPendingOrder(
                portfolio,
                symbol,
                stock.companyName(),
                OrderSide.SELL,
                quantity
        );

        long remainingQuantity =
                holding.getQuantity() - quantity;

        if (remainingQuantity == 0) {

            holdingRepository.delete(holding);

        } else {

            holding.setQuantity(
                    remainingQuantity
            );

            holdingRepository.save(holding);
        }

        BigDecimal updatedBalance =
                portfolio.getCashBalance()
                        .add(totalAmount)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        portfolio.setCashBalance(
                updatedBalance
        );

        portfolioRepository.save(portfolio);

        LocalDateTime executedAt =
                LocalDateTime.now();

        completeOrder(
                order,
                executionPrice,
                totalAmount,
                executedAt
        );

        createTransaction(
                portfolio,
                order,
                symbol,
                OrderSide.SELL,
                quantity,
                executionPrice,
                totalAmount,
                realizedPnL,
                executedAt
        );

        return new OrderResponse(
                order.getId(),
                symbol,
                stock.companyName(),
                OrderSide.SELL.name(),
                OrderType.MARKET.name(),
                OrderStatus.EXECUTED.name(),
                quantity,
                executionPrice,
                totalAmount,
                updatedBalance,
                executedAt,
                "Sell order executed successfully"
        );
    }

    private TradeOrder createPendingOrder(
            Portfolio portfolio,
            String symbol,
            String companyName,
            OrderSide side,
            long quantity) {

        TradeOrder order =
                new TradeOrder();

        order.setPortfolio(portfolio);
        order.setSymbol(symbol);
        order.setCompanyName(companyName);
        order.setSide(side);
        order.setOrderType(
                OrderType.MARKET
        );
        order.setStatus(
                OrderStatus.PENDING
        );
        order.setQuantity(quantity);

        return orderRepository.save(order);
    }

    private void completeOrder(
            TradeOrder order,
            BigDecimal executionPrice,
            BigDecimal totalAmount,
            LocalDateTime executedAt) {

        order.setStatus(
                OrderStatus.EXECUTED
        );

        order.setExecutionPrice(
                executionPrice
        );

        order.setTotalAmount(
                totalAmount
        );

        order.setExecutedAt(
                executedAt
        );

        orderRepository.save(order);
    }

    private void createTransaction(
            Portfolio portfolio,
            TradeOrder order,
            String symbol,
            OrderSide side,
            long quantity,
            BigDecimal executionPrice,
            BigDecimal totalAmount,
            BigDecimal realizedPnL,
            LocalDateTime executedAt) {

        TradeTransaction transaction =
                new TradeTransaction();

        transaction.setPortfolio(
                portfolio
        );

        transaction.setOrder(order);
        transaction.setSymbol(symbol);
        transaction.setSide(side);
        transaction.setQuantity(quantity);

        transaction.setPrice(
                executionPrice
        );

        transaction.setTotalAmount(
                totalAmount
        );

        transaction.setRealizedPnL(
                realizedPnL
        );

        transaction.setExecutedAt(
                executedAt
        );

        transactionRepository.save(
                transaction
        );
    }

    private void updateHoldingForBuy(
            Portfolio portfolio,
            String symbol,
            String companyName,
            long purchaseQuantity,
            BigDecimal purchasePrice) {

        Holding holding =
                holdingRepository
                        .findByPortfolioIdAndSymbol(
                                portfolio.getId(),
                                symbol
                        )
                        .orElse(null);

        if (holding == null) {

            Holding newHolding =
                    new Holding();

            newHolding.setPortfolio(
                    portfolio
            );

            newHolding.setSymbol(symbol);

            newHolding.setCompanyName(
                    companyName
            );

            newHolding.setQuantity(
                    purchaseQuantity
            );

            newHolding.setAverageBuyPrice(
                    purchasePrice.setScale(
                            4,
                            RoundingMode.HALF_UP
                    )
            );

            holdingRepository.save(
                    newHolding
            );

            return;
        }

        long existingQuantity =
                holding.getQuantity();

        long newQuantity =
                existingQuantity
                        + purchaseQuantity;

        BigDecimal existingInvestment =
                holding.getAverageBuyPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        existingQuantity
                                )
                        );

        BigDecimal newInvestment =
                purchasePrice.multiply(
                        BigDecimal.valueOf(
                                purchaseQuantity
                        )
                );

        BigDecimal newAveragePrice =
                existingInvestment
                        .add(newInvestment)
                        .divide(
                                BigDecimal.valueOf(
                                        newQuantity
                                ),
                                4,
                                RoundingMode.HALF_UP
                        );

        holding.setQuantity(
                newQuantity
        );

        holding.setAverageBuyPrice(
                newAveragePrice
        );

        holdingRepository.save(holding);
    }

    private BigDecimal calculateTotal(
            BigDecimal price,
            long quantity) {

        return price
                .multiply(
                        BigDecimal.valueOf(
                                quantity
                        )
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );
    }
}