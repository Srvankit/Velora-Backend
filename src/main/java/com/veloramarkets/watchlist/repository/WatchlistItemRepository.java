package com.veloramarkets.watchlist.repository;

import com.veloramarkets.watchlist.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistItemRepository
        extends JpaRepository<WatchlistItem, Long> {

    List<WatchlistItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<WatchlistItem> findByUserIdAndSymbol(
            Long userId,
            String symbol
    );

    boolean existsByUserIdAndSymbol(
            Long userId,
            String symbol
    );

    void deleteByUserIdAndSymbol(
            Long userId,
            String symbol
    );
}