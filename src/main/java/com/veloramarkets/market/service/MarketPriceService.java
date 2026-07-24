package com.veloramarkets.market.service;

import com.veloramarkets.common.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class MarketPriceService {

    private static final Map<String, MarketStock> STOCKS = Map.of(
            "RELIANCE", new MarketStock(
                    "Reliance Industries",
                    new BigDecimal("2985.50")
            ),
            "TCS", new MarketStock(
                    "Tata Consultancy Services",
                    new BigDecimal("3850.00")
            ),
            "INFY", new MarketStock(
                    "Infosys",
                    new BigDecimal("1625.75")
            ),
            "HDFCBANK", new MarketStock(
                    "HDFC Bank",
                    new BigDecimal("1710.25")
            ),
            "ICICIBANK", new MarketStock(
                    "ICICI Bank",
                    new BigDecimal("1255.40")
            )
    );

    public MarketStock getStock(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            throw new BadRequestException(
                    "Stock symbol is required"
            );
        }

        String normalizedSymbol =
                symbol.trim().toUpperCase();

        MarketStock stock = STOCKS.get(normalizedSymbol);

        if (stock == null) {
            throw new BadRequestException(
                    "Stock symbol is not supported"
            );
        }

        return stock;
    }

    public record MarketStock(
            String companyName,
            BigDecimal price
    ) {
    }
}