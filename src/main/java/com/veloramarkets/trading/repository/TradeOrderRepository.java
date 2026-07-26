package com.veloramarkets.trading.repository;

import com.veloramarkets.trading.entity.OrderStatus;
import com.veloramarkets.trading.entity.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TradeOrderRepository
        extends JpaRepository<TradeOrder, Long> {

    List<TradeOrder> findAllByPortfolioIdOrderByCreatedAtDesc(
            Long portfolioId
    );

    List<TradeOrder>
    findAllByPortfolioIdAndStatusOrderByCreatedAtDesc(
            Long portfolioId,
            OrderStatus status
    );

    List<TradeOrder>
    findTop5ByPortfolioIdOrderByCreatedAtDesc(
            Long portfolioId
    );

    long countByPortfolioId(
            Long portfolioId
    );

    Page<TradeOrder> findAllByPortfolioId(
            Long portfolioId,
            Pageable pageable
    );

}