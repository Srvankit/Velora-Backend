package com.veloramarkets.market.service;

import com.veloramarkets.common.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class MarketPriceService {

    /*
     * Temporary simulated market-price source.
     *
     * These prices are used by the trading engine until Velora Markets
     * is connected to a real-time market-data provider.
     */
    private static final Map<String, MarketStock> STOCKS = Map.ofEntries(

            // =========================
            // US STOCKS
            // =========================

            Map.entry(
                    "AAPL",
                    new MarketStock(
                            "Apple Inc.",
                            new BigDecimal("232.41")
                    )
            ),

            Map.entry(
                    "MSFT",
                    new MarketStock(
                            "Microsoft Corporation",
                            new BigDecimal("515.36")
                    )
            ),

            Map.entry(
                    "GOOGL",
                    new MarketStock(
                            "Alphabet Inc.",
                            new BigDecimal("193.18")
                    )
            ),

            Map.entry(
                    "AMZN",
                    new MarketStock(
                            "Amazon.com Inc.",
                            new BigDecimal("231.44")
                    )
            ),

            Map.entry(
                    "NVDA",
                    new MarketStock(
                            "NVIDIA Corporation",
                            new BigDecimal("173.50")
                    )
            ),

            Map.entry(
                    "META",
                    new MarketStock(
                            "Meta Platforms Inc.",
                            new BigDecimal("714.80")
                    )
            ),

            Map.entry(
                    "TSLA",
                    new MarketStock(
                            "Tesla Inc.",
                            new BigDecimal("316.06")
                    )
            ),

            Map.entry(
                    "NFLX",
                    new MarketStock(
                            "Netflix Inc.",
                            new BigDecimal("1180.00")
                    )
            ),

            // =========================
            // INDIAN STOCKS
            // =========================

            Map.entry(
                    "RELIANCE",
                    new MarketStock(
                            "Reliance Industries",
                            new BigDecimal("2985.50")
                    )
            ),

            Map.entry(
                    "TCS",
                    new MarketStock(
                            "Tata Consultancy Services",
                            new BigDecimal("3850.00")
                    )
            ),

            Map.entry(
                    "INFY",
                    new MarketStock(
                            "Infosys",
                            new BigDecimal("1625.75")
                    )
            ),

            Map.entry(
                    "HDFCBANK",
                    new MarketStock(
                            "HDFC Bank",
                            new BigDecimal("1710.25")
                    )
            ),

            Map.entry(
                    "ICICIBANK",
                    new MarketStock(
                            "ICICI Bank",
                            new BigDecimal("1255.40")
                    )
            )
    );

    /**
     * Returns market information for a supported stock symbol.
     */
    public MarketStock getStock(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            throw new BadRequestException(
                    "Stock symbol is required"
            );
        }

        String normalizedSymbol =
                symbol.trim().toUpperCase();

        MarketStock stock =
                STOCKS.get(normalizedSymbol);

        if (stock == null) {
            throw new BadRequestException(
                    "Stock symbol is not supported: "
                            + normalizedSymbol
            );
        }

        return stock;
    }

    /**
     * Returns true when Velora currently supports the supplied symbol.
     */
    public boolean isSupported(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            return false;
        }

        return STOCKS.containsKey(
                symbol.trim().toUpperCase()
        );
    }

    /**
     * Immutable representation of the simulated market data used by
     * the trading engine.
     */
    public record MarketStock(
            String companyName,
            BigDecimal price
    ) {
    }
}