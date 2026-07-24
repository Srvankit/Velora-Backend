package com.veloramarkets.trading.repository;

import com.veloramarkets.trading.entity.TradeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veloramarkets.trading.entity.OrderSide;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

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
    @Query("""
       SELECT COALESCE(SUM(t.realizedPnL), 0)
       FROM TradeTransaction t
       WHERE t.portfolio.id = :portfolioId
       AND t.side = :side
       """)
    BigDecimal sumRealizedPnLByPortfolioIdAndSide(
            @Param("portfolioId") Long portfolioId,
            @Param("side") OrderSide side
    );
}