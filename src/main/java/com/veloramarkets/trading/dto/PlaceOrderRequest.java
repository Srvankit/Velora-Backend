package com.veloramarkets.trading.dto;

import com.veloramarkets.trading.entity.OrderSide;
import com.veloramarkets.trading.entity.OrderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(

        @NotBlank(message = "Stock symbol is required")
        @Size(max = 20)
        String symbol,

        @NotNull(message = "Order side is required")
        OrderSide side,

        @NotNull(message = "Order type is required")
        OrderType orderType,

        @Min(
                value = 1,
                message = "Quantity must be at least 1"
        )
        long quantity

) {
}