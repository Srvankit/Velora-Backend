package com.veloramarkets.portfolio.service;

import com.veloramarkets.portfolio.dto.HoldingResponse;
import com.veloramarkets.portfolio.dto.PortfolioResponse;
import com.veloramarkets.portfolio.entity.Portfolio;
import com.veloramarkets.portfolio.repository.PortfolioRepository;
import com.veloramarkets.user.entity.User;
import com.veloramarkets.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PortfolioService {

    private static final BigDecimal INITIAL_CASH_BALANCE =
            new BigDecimal("100000.00");

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            UserRepository userRepository) {

        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
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

        List<HoldingResponse> holdings =
                portfolio.getHoldings()
                        .stream()
                        .map(holding ->
                                new HoldingResponse(
                                        holding.getId(),
                                        holding.getSymbol(),
                                        holding.getCompanyName(),
                                        holding.getQuantity(),
                                        holding.getAverageBuyPrice()
                                )
                        )
                        .toList();

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getCashBalance(),
                holdings.size(),
                holdings,
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }
}