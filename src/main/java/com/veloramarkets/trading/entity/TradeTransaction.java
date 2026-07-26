package com.veloramarkets.trading.entity;

import com.veloramarkets.portfolio.entity.Portfolio;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "trade_transactions",
        indexes = {
                @Index(
                        name = "idx_transactions_portfolio",
                        columnList = "portfolio_id"
                ),
                @Index(
                        name = "idx_transactions_symbol",
                        columnList = "symbol"
                ),
                @Index(
                        name = "idx_transactions_portfolio_executed",
                        columnList = "portfolio_id, executed_at"
                )
        }
)
public class TradeTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "portfolio_id",
            nullable = false
    )
    private Portfolio portfolio;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            unique = true
    )
    private TradeOrder order;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side;

    @Column(nullable = false)
    private long quantity;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal price;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Column(
            precision = 19,
            scale = 2
    )
    private BigDecimal realizedPnL;

    @Column(nullable = false, updatable = false)
    private LocalDateTime executedAt;

    public TradeTransaction() {
    }

    @PrePersist
    protected void onCreate() {

        if (executedAt == null) {
            executedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public TradeOrder getOrder() {
        return order;
    }

    public void setOrder(TradeOrder order) {
        this.order = order;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getRealizedPnL() {
        return realizedPnL;
    }

    public void setRealizedPnL(BigDecimal realizedPnL) {
        this.realizedPnL = realizedPnL;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}