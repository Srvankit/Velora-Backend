package com.veloramarkets.portfolio.repository;

import com.veloramarkets.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository
        extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUserId(Long userId);

    Optional<Portfolio> findByUserEmail(String email);

    boolean existsByUserId(Long userId);
}