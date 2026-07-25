package com.veloramarkets.wallet.service;

import com.veloramarkets.common.exception.BadRequestException;
import com.veloramarkets.portfolio.entity.Portfolio;
import com.veloramarkets.portfolio.repository.PortfolioRepository;
import com.veloramarkets.user.entity.User;
import com.veloramarkets.user.repository.UserRepository;
import com.veloramarkets.wallet.dto.WalletResponse;
import com.veloramarkets.wallet.dto.WalletTransactionResponse;
import com.veloramarkets.wallet.entity.WalletTransaction;
import com.veloramarkets.wallet.entity.WalletTransactionType;
import com.veloramarkets.wallet.repository.WalletTransactionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletService(
            UserRepository userRepository,
            PortfolioRepository portfolioRepository,
            WalletTransactionRepository walletTransactionRepository
    ) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        Portfolio portfolio = getPortfolio(user);

        return new WalletResponse(
                portfolio.getCashBalance()
        );
    }

    @Transactional
    public WalletResponse deposit(
            Authentication authentication,
            BigDecimal amount
    ) {

        validateAmount(amount);

        User user = getAuthenticatedUser(authentication);
        Portfolio portfolio = getPortfolio(user);

        BigDecimal newBalance =
                portfolio.getCashBalance().add(amount);

        portfolio.setCashBalance(newBalance);
        portfolioRepository.save(portfolio);

        saveTransaction(
                user,
                WalletTransactionType.DEPOSIT,
                amount,
                newBalance
        );

        return new WalletResponse(newBalance);
    }

    @Transactional
    public WalletResponse withdraw(
            Authentication authentication,
            BigDecimal amount
    ) {

        validateAmount(amount);

        User user = getAuthenticatedUser(authentication);
        Portfolio portfolio = getPortfolio(user);

        if (portfolio.getCashBalance().compareTo(amount) < 0) {
            throw new BadRequestException(
                    "Insufficient wallet balance"
            );
        }

        BigDecimal newBalance =
                portfolio.getCashBalance().subtract(amount);

        portfolio.setCashBalance(newBalance);
        portfolioRepository.save(portfolio);

        saveTransaction(
                user,
                WalletTransactionType.WITHDRAWAL,
                amount,
                newBalance
        );

        return new WalletResponse(newBalance);
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactions(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        return walletTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(transaction ->
                        new WalletTransactionResponse(
                                transaction.getId(),
                                transaction.getType(),
                                transaction.getAmount(),
                                transaction.getBalanceAfter(),
                                transaction.getCreatedAt()
                        )
                )
                .toList();
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );
    }

    private Portfolio getPortfolio(User user) {

        return portfolioRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Portfolio not found"
                        )
                );
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BadRequestException(
                    "Amount must be greater than zero"
            );
        }
    }

    private void saveTransaction(
            User user,
            WalletTransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter
    ) {

        WalletTransaction transaction =
                new WalletTransaction();

        transaction.setUser(user);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);

        walletTransactionRepository.save(transaction);
    }
}