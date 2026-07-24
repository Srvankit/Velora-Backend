package com.veloramarkets.portfolio.repository;

import com.veloramarkets.portfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository
        extends JpaRepository<Holding, Long> {

    List<Holding> findAllByPortfolioId(Long portfolioId);

    Optional<Holding> findByPortfolioIdAndSymbol(
            Long portfolioId,
            String symbol
    );

    boolean existsByPortfolioIdAndSymbol(
            Long portfolioId,
            String symbol
    );
}