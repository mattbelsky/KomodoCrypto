package komodocrypto.model.arbitrage;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ArbitrageModel {

    private int id;
    private Timestamp timestamp;
    private BigDecimal amount;
    private CurrencyPair currencyPair;
    private String currencyPairName;
    private BigDecimal difference;
    private BigDecimal highBid;
    private Exchange highBidExchange;
    private String highBidExchangeName;
    private BigDecimal lowAsk;
    private Exchange lowAskExchange;
    private String lowAskExchangeName;
    private String lowAskExchangeWalletAddr;

    public ArbitrageModel(Builder builder) {
        this.timestamp = builder.timestamp;
        this.amount = builder.amount;
        this.highBid = builder.highBid;
        this.lowAsk = builder.lowAsk;
        this.difference = builder.difference;
        this.currencyPair = builder.currencyPair;
        this.currencyPairName = builder.currencyPairName;
        this.highBidExchange = builder.highBidExchange;
        this.highBidExchangeName = builder.highBidExchangeName;
        this.lowAskExchange = builder.lowAskExchange;
        this.lowAskExchangeName = builder.lowAskExchangeName;
        this.lowAskExchangeWalletAddr = builder.lowAskExchangeWalletAddr;
    }

    public ArbitrageModel() {}

    public int getId() {
        return id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
    }

    public String getCurrencyPairName() {
        return currencyPairName;
    }

    public void setCurrencyPairName(String currencyPairName) {
        this.currencyPairName = currencyPairName;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }

    public BigDecimal getHighBid() {
        return highBid;
    }

    public void setHighBid(BigDecimal highBid) {
        this.highBid = highBid;
    }

    public Exchange getHighBidExchange() {
        return highBidExchange;
    }

    public void setHighBidExchange(Exchange highBidExchange) {
        this.highBidExchange = highBidExchange;
    }

    public String getHighBidExchangeName() {
        return highBidExchangeName;
    }

    public void setHighBidExchangeName(String highBidExchangeName) {
        this.highBidExchangeName = highBidExchangeName;
    }

    public BigDecimal getLowAsk() {
        return lowAsk;
    }

    public void setLowAsk(BigDecimal lowAsk) {
        this.lowAsk = lowAsk;
    }

    public Exchange getLowAskExchange() {
        return lowAskExchange;
    }

    public void setLowAskExchange(Exchange lowAskExchange) {
        this.lowAskExchange = lowAskExchange;
    }

    public String getLowAskExchangeName() {
        return lowAskExchangeName;
    }

    public void setLowAskExchangeName(String lowAskExchangeName) {
        this.lowAskExchangeName = lowAskExchangeName;
    }

    public String getLowAskExchangeWalletAddr() {
        return lowAskExchangeWalletAddr;
    }

    public void setLowAskExchangeWalletAddr(String lowAskExchangeWalletAddr) {
        this.lowAskExchangeWalletAddr = lowAskExchangeWalletAddr;
    }

    public static class Builder {

        private Timestamp timestamp;
        private BigDecimal amount;
        private CurrencyPair currencyPair;
        private String currencyPairName;
        private BigDecimal difference;
        private BigDecimal highBid;
        private Exchange highBidExchange;
        private String highBidExchangeName;
        private BigDecimal lowAsk;
        private Exchange lowAskExchange;
        private String lowAskExchangeName;
        private String lowAskExchangeWalletAddr;

        public Builder timestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder highBid(BigDecimal highBid) {
            this.highBid = highBid;
            return this;
        }

        public Builder lowAsk(BigDecimal lowAsk) {
            this.lowAsk = lowAsk;
            return this;
        }

        public Builder difference() {
            if (lowAsk != null && highBid != null)
                this.difference = lowAsk.subtract(highBid);
            else
                this.difference = BigDecimal.ZERO;
            return this;
        }

        public Builder currencyPair(CurrencyPair currencyPair) {
            this.currencyPair = currencyPair;
            this.currencyPairName = currencyPair.toString();
            return this;
        }

        public Builder highBidExchange(Exchange highBidExchange) {
            this.highBidExchange = highBidExchange;
            this.highBidExchangeName = highBidExchange.getExchangeSpecification().getExchangeName();
            return this;
        }

        public Builder lowAskExchange(Exchange lowAskExchange) {
            this.lowAskExchange = lowAskExchange;
            this.lowAskExchangeName = lowAskExchange.getExchangeSpecification().getExchangeName();
            return this;
        }

        public Builder lowAskExchangeWalletAddress(String lowAskExchangeWalletAddr) {
            this.lowAskExchangeWalletAddr = lowAskExchangeWalletAddr;
            return this;
        }

        public ArbitrageModel build() {
            return new ArbitrageModel(this);
        }
    }
}
