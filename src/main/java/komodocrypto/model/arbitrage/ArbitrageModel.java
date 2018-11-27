package komodocrypto.model.arbitrage;

import komodocrypto.model.TradeModel;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ArbitrageModel extends TradeModel {

    private int id;
    private Timestamp timestamp;
    private CurrencyPair currencyPair;
    private String currencyPairName;
    private BigDecimal difference;
    private BigDecimal highBid;
    private Exchange highBidExchange;
    private String highBidExchangeName;
    private BigDecimal lowAsk;
    private Exchange lowAskExchange;
    private String lowAskExchangeName;
    private String highBidExchangeWalletId;
    private String lowAskExchangeWalletId;

    public ArbitrageModel(Timestamp timestamp, String currencyPairName, BigDecimal difference, BigDecimal lowAsk, String lowAskExchangeName, BigDecimal highBid, String highBidExchangeName) {
        this.timestamp = timestamp;
        this.currencyPairName = currencyPairName;
        this.difference = difference;
        this.lowAsk = lowAsk;
        this.lowAskExchangeName = lowAskExchangeName;
        this.highBid = highBid;
        this.highBidExchangeName = highBidExchangeName;
    }

    public ArbitrageModel() {
    }

    public int getId() {
        return id;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    @Override
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

    public String getHighBidExchangeWalletId() {
        return highBidExchangeWalletId;
    }

    public void setHighBidExchangeWalletId(String highBidExchangeWalletId) {
        this.highBidExchangeWalletId = highBidExchangeWalletId;
    }

    public String getLowAskExchangeWalletId() {
        return lowAskExchangeWalletId;
    }

    public void setLowAskExchangeWalletId(String lowAskExchangeWalletId) {
        this.lowAskExchangeWalletId = lowAskExchangeWalletId;
    }
}
