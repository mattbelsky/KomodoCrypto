package komodocrypto.services.trades;

import komodocrypto.mappers.ExchangeMapper;
import komodocrypto.model.TradeModel;
import komodocrypto.model.exchanges.ExchangeInOutLimits;
import komodocrypto.model.exchanges.ExchangeModel;
import komodocrypto.services.exchanges.ExchangeService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service("BaseTradeService")
public class BaseTradeService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    // This value is multiplied by any trading fees in case the client's wallet has an insufficient balance due to
    // miscalculating the fees.
    private final BigDecimal SAFETY_BUFFER = new BigDecimal(1.5);

    @Autowired
    ExchangeMapper exchangeMapper;

    @Autowired
    ExchangeService exchangeService;

    public MarketOrder buildMarketOrder(TradeModel tradeModel)
            throws IOException {

        Exchange exchange = tradeModel.getExchange();
        Currency base = tradeModel.getCurrencyPair().base;
        CurrencyPair currencyPair = tradeModel.getCurrencyPair();
        BigDecimal amount = tradeModel.getAmount();
        BigDecimal tradingFee = calculateTradingFees(exchange, currencyPair, amount);
        Order.OrderType orderType = tradeModel.getOrderType();
        BigDecimal balance = exchange.getAccountService().getAccountInfo().getWallet().getBalance(base).getAvailable();

        if (!hasSufficientFunds(amount, balance, tradingFee)) {
            logger.warn("Insufficient funds for " + base.getCurrencyCode() + " in " +
                    exchange.getExchangeSpecification().getExchangeName() + " wallet.");
            return null;
        }

        logger.info("Market order built to trade " + currencyPair.toString() + " on " +
                exchange.getExchangeSpecification().getExchangeName() + ".");
        return new MarketOrder(orderType, amount, currencyPair);
    }

    public String makeMarketTrade(TradeModel tradeModel, MarketOrder order) throws IOException {

        Exchange exchange = tradeModel.getExchange();
        String transactionId = exchange.getTradeService().placeMarketOrder(order);
        logger.info("Market order completed.");
        return transactionId;
    }

    public TradeModel buildTradeModel(String exchangeName, String base, String counter, double amountDouble, Order.OrderType orderType) {

        Exchange exchange = exchangeService.createExchange(exchangeName);
        CurrencyPair currencyPair = new CurrencyPair(base, counter);
        BigDecimal amount = new BigDecimal(amountDouble);
        String walletAddr = exchangeService.getWalletAddress(exchangeName, base);

        // Builds the trade model.
        TradeModel tradeModel = new TradeModel.Builder()
                .currencyPair(currencyPair)
                .exchange(exchange)
                .amount(amount)
                .walletAddress(walletAddr)
                .orderType(orderType)
                .build();

        return tradeModel;
    }

    // Returns true if the wallet's balance is greater than the fees charged for the transaction.
    public boolean hasSufficientFunds(BigDecimal amount, BigDecimal balance, BigDecimal fees) {

        BigDecimal totalTradeCost = amount.add(fees).add(SAFETY_BUFFER);
        return balance.compareTo(totalTradeCost) == 1;
    }

    public BigDecimal calculateTradingFees(Exchange exchange, CurrencyPair cp, BigDecimal amount) {

        String exchangeName = exchange.getExchangeSpecification().getExchangeName();
        String currencyCode = cp.base.getCurrencyCode();

        ExchangeModel exchangeModel = exchangeMapper.getExchangeModelByName(exchangeName);
        List<ExchangeInOutLimits> exchangeLimits = new ArrayList<>();
        exchangeLimits.add(exchangeMapper.getLimitsbyExchangeAndCurrency(exchangeName, currencyCode));
        exchangeModel.setExchangeLimits(exchangeLimits);

        BigDecimal fee = exchangeModel.getTakerTradeFee().multiply(amount);
        return fee;
    }
}
