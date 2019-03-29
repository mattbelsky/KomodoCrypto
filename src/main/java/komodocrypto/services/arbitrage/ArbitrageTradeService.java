package komodocrypto.services.arbitrage;

import komodocrypto.exceptions.custom_exceptions.InsufficientFundsException;
import komodocrypto.model.arbitrage.ArbitrageModel;
import komodocrypto.services.trades.BaseTradeService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service("ArbitrageTradeService")
public class ArbitrageTradeService extends BaseTradeService {

    Logger logger = LoggerFactory.getLogger(getClass());

    public MarketOrder[] buildArbitrageMarketOrders(ArbitrageModel arbitrageModel)
            throws IOException, InsufficientFundsException {

        BigDecimal amount = arbitrageModel.getAmount();
        CurrencyPair cp = arbitrageModel.getCurrencyPair();
        Currency fromCurrency = cp.base;
        Currency toCurrency = cp.counter;
        Exchange sellExchange = arbitrageModel.getHighBidExchange();
        Exchange buyExchange = arbitrageModel.getLowAskExchange();
        MarketOrder[] orders = new MarketOrder[2];

        // Gets the fees each exchange charges for the respective transactions made on them and adds them to the
        // market order object.
        BigDecimal sellFee = calculateTradingFees(sellExchange, cp, amount);
        BigDecimal buyFee = calculateTradingFees(buyExchange, cp, amount);
        BigDecimal totalFee = sellFee.add(buyFee);

        // Checks wallet for the selling exchange/currency to ensure there are enough funds to make the trade profitable.
        BigDecimal fromWalletBalance = sellExchange.getAccountService().getAccountInfo().getWallet().getBalance(fromCurrency).getAvailable();

        if (!hasSufficientFunds(BigDecimal.ZERO, fromWalletBalance, totalFee)) {
            String message = "Insufficient funds to trade.";
            logger.error(message);
            throw new InsufficientFundsException(message, HttpStatus.BAD_REQUEST);
        } else {
            orders[0] = new MarketOrder(Order.OrderType.ASK, amount, cp);
            orders[1] = new MarketOrder(Order.OrderType.BID, amount, new CurrencyPair(toCurrency, fromCurrency));
            logger.info("Built market orders.");
        }

        return orders;
    }

    // Should be in a separate thread as a delay between selling and buying is involved... Or should it?
    public void makeArbitrageMarketTrades(ArbitrageModel arbitrageModel, MarketOrder[] orders)
            throws IOException {

        BigDecimal amount = arbitrageModel.getAmount();
        Exchange fromExchange = arbitrageModel.getHighBidExchange();
        Exchange toExchange = arbitrageModel.getLowAskExchange();
        Currency fromCurrency = arbitrageModel.getCurrencyPair().base;
        Currency toCurrency = arbitrageModel.getCurrencyPair().counter;
        String walletAddr = arbitrageModel.getLowAskExchangeWalletAddr();
        BigDecimal initBalanceBase = fromExchange.getAccountService().getAccountInfo().getWallet().getBalance(fromCurrency).getAvailable();
        BigDecimal initBalanceCounter = toExchange.getAccountService().getAccountInfo().getWallet().getBalance(toCurrency).getAvailable();

        // Places the market order to sell.
        fromExchange.getTradeService().placeMarketOrder(orders[0]);
        logger.info("Placing market order to sell...");

        // Gives the transaction time to complete by continuously checking whether the available balance has changed.
        while(fromExchange.getAccountService().getAccountInfo().getWallet().getBalance(fromCurrency).getAvailable()
                .compareTo(initBalanceBase) == 0)
            continue;
        logger.info("Market sell order completed.");

        // Transfers the funds to the exchange to buy from.
        fromExchange.getAccountService().withdrawFunds(toCurrency, amount, walletAddr);
        logger.info("Transferring funds to next exchange's wallet...");

        // Gives the withdrawal to the wallet on the new exchange time to complete by continuously checking whether its
        // balance has changed.
        while (toExchange.getAccountService().getAccountInfo().getWallet().getBalance(toCurrency).getAvailable()
                .compareTo(initBalanceCounter) == 0)
            continue;
        logger.info("Transfer completed.");

        // Places the market order to buy.
        toExchange.getTradeService().placeMarketOrder(orders[1]);
        logger.info("Placed market order to buy. (No message upon completion).");
    }
}
