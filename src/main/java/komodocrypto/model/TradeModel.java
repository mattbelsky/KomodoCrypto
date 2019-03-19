package komodocrypto.model;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TradeModel {

    private int id;
    private Timestamp timestamp;
    private CurrencyPair currencyPair;
    private Exchange exchange;
    private BigDecimal amount;
    private String walletAddr;
    private Order.OrderType orderType;

    private TradeModel(Builder builder) {
        this.timestamp = builder.timestamp;
        this.currencyPair = builder.currencyPair;
        this.exchange = builder.exchange;
        this.amount = builder.amount;
        this.walletAddr = builder.walletAddr;
        this.orderType = builder.orderType;
    }

    public TradeModel() {
    }

    public int getId() {
        return id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getWalletAddr() {
        return walletAddr;
    }

    public void setWalletAddr(String walletAddr) {
        this.walletAddr = walletAddr;
    }

    public Order.OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(Order.OrderType orderType) {
        this.orderType = orderType;
    }

    public static class Builder {

        private Timestamp timestamp;
        private CurrencyPair currencyPair;
        private Exchange exchange;
        private BigDecimal amount;
        private String walletAddr;
        private Order.OrderType orderType;

        public Builder timestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder currencyPair(CurrencyPair currencyPair) {
            this.currencyPair = currencyPair;
            return this;
        }

        public Builder exchange(Exchange exchange) {
            this.exchange = exchange;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder walletAddress(String walletAddr) {
            this.walletAddr = walletAddr;
            return this;
        }

        public Builder orderType(Order.OrderType orderType) {
            this.orderType = orderType;
            return this;
        }

        public TradeModel build() {
            return new TradeModel(this);
        }
    }
}
