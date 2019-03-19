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

//    //    creates a fake user so mvc views can work
//    public User createTempUser() {
//        User user = new User();
//        user.setFirst_name("Unicorn");
//        user.setLast_name("Badger");
//        user.setEmail("UnicornBadger@fake.com");
//        user.setPassword("password");
//        return user;
//    }

//    public ArbitrageOutput makeMarketTrade(String exchangeHigh, String exchangeLow, String currencyPair,
//                                           BigDecimal amount) throws ExchangeConnectionException {
//        // Set metadata
//        ArbitrageOutput arbitrageTrade = new ArbitrageOutput();
//        arbitrageTrade.setExchangeHigh(exchangeHigh);
//        arbitrageTrade.setExchangeLow(exchangeLow);
//        arbitrageTrade.setCurrencyPair(currencyPair);
//        arbitrageTrade.setAmount(amount);
//
//        // Create list of trade data
//        List<TradeDetails> trades = new ArrayList<>();
//
//        // Make high trade
//        TradeDetails tradeHigh = tradeExchange(exchangeHigh, currencyPair, amount);
//
//        // Swap currency pair order then make low trade
//        String currencyPairLow = flipCurrencyPair(currencyPair);
//        TradeDetails tradeLow = tradeExchange(exchangeLow,currencyPairLow,amount);
//
//        trades.add(tradeHigh);
//        trades.add(tradeLow);
//        arbitrageTrade.setTradeDetails(trades);
//        return arbitrageTrade;
//    }

//    private TradeDetails tradeExchange(String exchange, String currencyPair, BigDecimal amount)
//            throws ExchangeConnectionException {
//        switch (exchange.toLowerCase()) {
//            case "bitstamp": {
//                // Connect to ExchangeData
//                ExchangeData bitstamp = bitstampUtil.createExchange();
//                TradeService tradeService = bitstamp.getTradeService();
//                TradeDetails trade = new TradeDetails();
//                trade.setExchange("Bitstamp");
//
//                // Place market order
//                MarketOrder marketOrder = convertXchangeTrade(currencyPair, amount);
//                logger.info("Attempting Bitstamp market order...");
//                try {
//                    String marketOrderReturnValue = tradeService.placeMarketOrder(marketOrder);
//                    trade.setTimestamp(System.currentTimeMillis());
//                    trade.setOrderId(marketOrderReturnValue);
//                    logger.info("Bitstamp order successfully placed.");
//                } catch (IOException e) {
//                    throw new ExchangeConnectionException("Unable to place order", HttpStatus.BAD_REQUEST);
//                }
//                return trade;
//            }
//            case "binance": {
//                // Connect to ExchangeData
//                BinanceApiRestClient client = binanceUtil.createExchange();
//                TradeDetails trade = new TradeDetails();
//                trade.setExchange("Binance");
//
//                // Place market order
//                logger.info("Attempting Binance market order...");
//                NewOrderResponse newOrderResponse = client.newOrder(
//                        marketBuy(currencyPair, amount.toString()).newOrderRespType(NewOrderResponseType.FULL));
//                trade.setTimestamp(System.currentTimeMillis());
//                trade.setOrderId(newOrderResponse.getClientOrderId());
//                logger.info("Binance order successfully placed.");
//                return trade;
//            }
//
////            case "bittrex": {
////
////            }
////            case "kraken": {
////
////            }
////            case "gdax": {
////
////            }
//            default:
//                throw new ExchangeConnectionException("Invalid ExchangeData", HttpStatus.BAD_REQUEST);
//        }
//
//    }
}
