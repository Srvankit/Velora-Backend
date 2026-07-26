package com.veloramarkets.market.service;

import com.veloramarkets.common.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import java.util.List;
import java.util.Locale;

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
                            new BigDecimal("232.41"),
                            new BigDecimal("229.23"),
                            48_120_000L,
                            "NASDAQ",
                            "Technology"
                    )
            ),

            Map.entry(
                    "MSFT",
                    new MarketStock(
                            "Microsoft Corporation",
                            new BigDecimal("515.36"),
                            new BigDecimal("510.20"),
                            21_450_000L,
                            "NASDAQ",
                            "Technology"
                    )
            ),

            Map.entry(
                    "GOOGL",
                    new MarketStock(
                            "Alphabet Inc.",
                            new BigDecimal("193.18"),
                            new BigDecimal("195.40"),
                            32_800_000L,
                            "NASDAQ",
                            "Communication Services"
                    )
            ),

            Map.entry(
                    "AMZN",
                    new MarketStock(
                            "Amazon.com Inc.",
                            new BigDecimal("231.44"),
                            new BigDecimal("228.60"),
                            41_300_000L,
                            "NASDAQ",
                            "Consumer Cyclical"
                    )
            ),

            Map.entry(
                    "NVDA",
                    new MarketStock(
                            "NVIDIA Corporation",
                            new BigDecimal("173.50"),
                            new BigDecimal("168.80"),
                            182_400_000L,
                            "NASDAQ",
                            "Technology"
                    )
            ),

            Map.entry(
                    "META",
                    new MarketStock(
                            "Meta Platforms Inc.",
                            new BigDecimal("714.80"),
                            new BigDecimal("720.50"),
                            18_750_000L,
                            "NASDAQ",
                            "Communication Services"
                    )
            ),

            Map.entry(
                    "TSLA",
                    new MarketStock(
                            "Tesla Inc.",
                            new BigDecimal("316.06"),
                            new BigDecimal("321.80"),
                            96_700_000L,
                            "NASDAQ",
                            "Consumer Cyclical"
                    )
            ),

            Map.entry(
                    "NFLX",
                    new MarketStock(
                            "Netflix Inc.",
                            new BigDecimal("1180.00"),
                            new BigDecimal("1162.50"),
                            6_850_000L,
                            "NASDAQ",
                            "Communication Services"
                    )
            ),

            // =========================
            // INDIAN STOCKS
            // =========================

            Map.entry(
                    "RELIANCE",
                    new MarketStock(
                            "Reliance Industries",
                            new BigDecimal("2985.50"),
                            new BigDecimal("2950.00"),
                            9_850_000L,
                            "NSE",
                            "Energy"
                    )
            ),

            Map.entry(
                    "TCS",
                    new MarketStock(
                            "Tata Consultancy Services",
                            new BigDecimal("3850.00"),
                            new BigDecimal("3875.50"),
                            3_420_000L,
                            "NSE",
                            "Technology"
                    )
            ),

            Map.entry(
                    "INFY",
                    new MarketStock(
                            "Infosys",
                            new BigDecimal("1625.75"),
                            new BigDecimal("1608.20"),
                            8_750_000L,
                            "NSE",
                            "Technology"
                    )
            ),

            Map.entry(
                    "HDFCBANK",
                    new MarketStock(
                            "HDFC Bank",
                            new BigDecimal("1710.25"),
                            new BigDecimal("1724.60"),
                            12_650_000L,
                            "NSE",
                            "Financial Services"
                    )
            ),

            Map.entry(
                    "ICICIBANK",
                    new MarketStock(
                            "ICICI Bank",
                            new BigDecimal("1255.40"),
                            new BigDecimal("1240.10"),
                            15_300_000L,
                            "NSE",
                            "Financial Services"
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
     * Returns all stocks currently supported by Velora Markets.
     */
    public List<MarketStockEntry> getAllStocks() {

        return STOCKS.entrySet()
                .stream()
                .map(entry -> {

                    MarketStock stock = entry.getValue();

                    return new MarketStockEntry(
                            entry.getKey(),
                            stock.companyName(),
                            stock.price(),
                            stock.previousClose(),
                            stock.change(),
                            stock.changePercent(),
                            stock.volume(),
                            stock.exchange(),
                            stock.sector()
                    );
                })
                .sorted((a, b) ->
                        a.symbol().compareTo(b.symbol())
                )
                .toList();
    }

    /**
     * Searches supported stocks by symbol or company name.
     */
    public List<MarketStockEntry> searchStocks(
            String query
    ) {

        if (query == null || query.isBlank()) {
            return getAllStocks();
        }

        String normalizedQuery =
                query.trim().toLowerCase(Locale.ROOT);

        return getAllStocks()
                .stream()
                .filter(stock ->
                        stock.symbol()
                                .toLowerCase(Locale.ROOT)
                                .contains(normalizedQuery)
                                ||
                                stock.companyName()
                                        .toLowerCase(Locale.ROOT)
                                        .contains(normalizedQuery)
                )
                .toList();
    }

    /**
     * Returns stocks ordered by highest percentage gain.
     */
    public List<MarketStockEntry> getTopGainers() {

        return getAllStocks()
                .stream()
                .filter(stock ->
                        stock.changePercent()
                                .compareTo(BigDecimal.ZERO) > 0
                )
                .sorted((a, b) ->
                        b.changePercent()
                                .compareTo(a.changePercent())
                )
                .toList();
    }

    /**
     * Returns stocks ordered by largest percentage loss.
     */
    public List<MarketStockEntry> getTopLosers() {

        return getAllStocks()
                .stream()
                .filter(stock ->
                        stock.changePercent()
                                .compareTo(BigDecimal.ZERO) < 0
                )
                .sorted((a, b) ->
                        a.changePercent()
                                .compareTo(b.changePercent())
                )
                .toList();
    }

    /**
     * Returns stocks ordered by highest trading volume.
     */
    public List<MarketStockEntry> getMostActive() {

        return getAllStocks()
                .stream()
                .sorted((a, b) ->
                        Long.compare(
                                b.volume(),
                                a.volume()
                        )
                )
                .toList();
    }

    public record MarketStockEntry(
            String symbol,
            String companyName,
            BigDecimal price,
            BigDecimal previousClose,
            BigDecimal change,
            BigDecimal changePercent,
            long volume,
            String exchange,
            String sector
    ) {
    }

    /**
     * Immutable representation of the simulated market data used by
     * the trading engine.
     */
    public record MarketStock(
            String companyName,
            BigDecimal price,
            BigDecimal previousClose,
            long volume,
            String exchange,
            String sector
    ) {

        public BigDecimal change() {
            return price.subtract(previousClose);
        }

        public BigDecimal changePercent() {

            if (previousClose.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            return change()
                    .multiply(new BigDecimal("100"))
                    .divide(
                            previousClose,
                            2,
                            java.math.RoundingMode.HALF_UP
                    );
        }
    }
}