package com.veloramarkets.trading.repository;

import com.veloramarkets.trading.entity.TradeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeTransactionRepository
        extends JpaRepository<TradeTransaction, Long> {

    List<TradeTransaction>
    findAllByPortfolioIdOrderByExecutedAtDesc(
            Long portfolioId
    );

    List<TradeTransaction>
    findAllByPortfolioIdAndSymbolOrderByExecutedAtDesc(
            Long portfolioId,
            String symbol
    );
}