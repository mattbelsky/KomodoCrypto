package komodocrypto.services.arbitrage;

//import com.binance.api.client.BinanceApiRestClient;
//import com.binance.api.client.domain.account.NewOrderResponse;
//import com.binance.api.client.domain.account.NewOrderResponseType;
import com.sun.xml.internal.bind.v2.TODO;
import komodocrypto.configuration.ExchangesConfig;
import komodocrypto.exceptions.custom_exceptions.ExchangeConnectionException;
import komodocrypto.exceptions.custom_exceptions.InsufficientFundsException;
import komodocrypto.mappers.ArbitrageMapper;
import komodocrypto.model.arbitrage.ArbitrageModel;
import komodocrypto.model.arbitrage.ArbitrageOutput;
import komodocrypto.model.arbitrage.TradeDetails;
import komodocrypto.model.user.User;
import komodocrypto.services.exchanges.ExchangeService;
import komodocrypto.services.trades.BaseTradeService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//import static com.binance.api.client.domain.account.NewOrder.marketBuy;

@Service("ArbirageTradeService")
public class ArbitrageTradeService extends BaseTradeService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ExchangesConfig exchangesConfig;

    @Autowired
    ArbitrageMapper arbitrageMapper;

    @Autowired
    ExchangeService exchangeService;

//    @Autowired
//    BinanceUtil binanceUtil;

    /*
                ▪ If arbitrage opportunity between exchanges is identified,
                NOTE: We are coming to this with an ArbitrageModel
                    Timestamp timestamp;
                    String currencyPair;
                    BigDecimal difference;
                    BigDecimal lowAsk;
                    String lowAskExchange;
                    BigDecimal highBid;
                    String highBidExchange;
                • What is wallet address (need a wallet outside the exchanges in order to simplify transactions)?
                • Sell currency A for desired currency B in exchange with lowest bid
                    ◦ ex. Exchange A: ETH/BTC = 0.02/0.05
                    ◦ Exchange B: BTC/ETH = 22.03/22.06
                • Withdraw currency B to wallet or transfer directly to avoid getting doubly hit with mining fees
                • Buy currency A in exchange with highest ask
                • Verify transaction complete?
                • Persist the transaction
        ◦ Methods
            ▪ MarketOrder[] buildArbitrageMarketOrders(Exchange[] exchange, CurrencyPair cp, BigDecimal amount)
            ▪ String[] getExchangeWalletIds(Exchange[] exchange)
            ▪ void makeArbitrageMarketTrades(MarketOrder[] orders)
              String buildGetExchangeCurrencyWalletIdMethodName(String exchange, String currency)
     */

    public MarketOrder[] buildArbitrageMarketOrders(ArbitrageModel arbitrageModel)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException, InsufficientFundsException {

        CurrencyPair cp = arbitrageModel.getCurrencyPair();
        Currency fromCurrency = cp.base;
        Currency toCurrency = cp.counter;
        String[] exchangeWalletIds = getExchangeWalletIds(arbitrageModel);

        // Gets the balance of the wallet being sold from.
        BigDecimal fromWalletBalance = arbitrageModel
                .getHighBidExchange()
                .getAccountService()
                .getAccountInfo()
                .getWallet(exchangeWalletIds[0])
                .getBalance(fromCurrency)
                .getAvailable();

        MarketOrder sellOrder = new MarketOrder(Order.OrderType.ASK, fromWalletBalance, cp);
        // TODO How to handle the delay between sale processing and buy time? Does this new currency pair trade on desired exchange?
        MarketOrder buyOrder = new MarketOrder(Order.OrderType.BID, fromWalletBalance, new CurrencyPair(toCurrency, fromCurrency));

        Exchange sellExchange = arbitrageModel.getHighBidExchange();
        Exchange buyExchange = arbitrageModel.getLowAskExchange();

        // Gets the fees each exchange charges for the respective transactions made on them and adds them to the
        // market order object.
        BigDecimal sellFee = calculateTradingFees(sellExchange, cp, fromWalletBalance);
        BigDecimal buyFee = calculateTradingFees(buyExchange, cp, fromWalletBalance);
        BigDecimal totalFee = sellFee.add(buyFee);

        // Checks wallet for the selling exchange/currency to ensure there are enough funds to make the trade profitable.
        String walletId = getExchangeWalletIds(arbitrageModel)[0];
        Exchange ex = exchangeService.createExchange(sellExchange.getExchangeSpecification().getExchangeName());
        BigDecimal walletBalance = getClientWalletBalance(ex, fromCurrency, walletId);
        MarketOrder[] orders = new MarketOrder[2];

        if (!hasSufficientFunds(BigDecimal.ZERO, walletBalance, totalFee))
            throw new InsufficientFundsException("Insufficient funds to trade.", HttpStatus.BAD_REQUEST);

        else {
            orders[0] = sellOrder;
            orders[1] = buyOrder;
        }

        return orders;
    }

    // Gets the names of and reflexively calls the methods getting the desired exchange and currency wallet IDs.
    // TODO Is this method even needed??
    public String[] getExchangeWalletIds(ArbitrageModel arbitrageModel)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // TODO Fix possible problem where name is not formatted correctly (i.e. "binance" instead of "Binance").
        String exchangeFrom = arbitrageModel.getHighBidExchangeName();
        String exchangeTo = arbitrageModel.getLowAskExchangeName();
        String currencyFrom = arbitrageModel.getCurrencyPair().base.getSymbol();
        String currencyTo = arbitrageModel.getCurrencyPair().counter.getSymbol();

        String fromWalletMethodName = exchangeService.buildGetExchangeCurrencyWalletIdMethodName(exchangeFrom, currencyFrom);
        String toWalletMethodName = exchangeService.buildGetExchangeCurrencyWalletIdMethodName(exchangeTo, currencyTo);

        Method getFromWalletId = exchangesConfig.getClass().getMethod(fromWalletMethodName, null);
        Method getToWalletId = exchangesConfig.getClass().getMethod(toWalletMethodName, null);

        String[] exchangeWalletIds = new String[2];
        exchangeWalletIds[0] = getFromWalletId.invoke(exchangesConfig, null).toString();
        exchangeWalletIds[1] = getToWalletId.invoke(exchangesConfig, null).toString();

        // Sets the wallet id in the TradeModel and ArbitrageModel objects.
        arbitrageModel.setWalletId(exchangeWalletIds[0]);
        arbitrageModel.setHighBidExchangeWalletId(exchangeWalletIds[0]);
        arbitrageModel.setLowAskExchangeWalletId(exchangeWalletIds[1]);

        return exchangeWalletIds;
    }

    // Should be in a separate thread as a delay between selling and buying is involved... Or should it?
    public void makeArbitrageMarketTrades(ArbitrageModel arbitrageModel, Exchange[] exchanges, MarketOrder[] orders)
            throws IOException {

        Exchange fromExchange = exchangeService.createExchange(exchanges[0].getExchangeSpecification().getExchangeName());
        Exchange toExchange = exchangeService.createExchange(exchanges[1].getExchangeSpecification().getExchangeName());

        // Places the immediate market order to sell.
        TradeService sellExchangeTradeService = fromExchange.getTradeService();
        sellExchangeTradeService.placeMarketOrder(orders[0]);

        // Verifies the funds have been sold. Repeatedly queries the exchange until the balance of the specified wallet is 0.
        String fromWalletId = arbitrageModel.getHighBidExchangeWalletId();
        String toWalletId = arbitrageModel.getLowAskExchangeWalletId();
        Currency fromCurrency = arbitrageModel.getCurrencyPair().base;
        Currency toCurrency = arbitrageModel.getCurrencyPair().counter;

        // TODO Resolve this -- possible miniscule amount left over in account due to BigDecimal weirdness.
        while (getClientWalletBalance(fromExchange, fromCurrency, fromWalletId).compareTo(new BigDecimal(0)) > 0)
            continue;

        // Gets the balance of the wallet for the currency purchased.
        BigDecimal balance = getClientWalletBalance(fromExchange, toCurrency, toWalletId);

        // Transfers the funds to the exchange to buy from.
        fromExchange.getAccountService().withdrawFunds(toCurrency, balance, toWalletId);

        // Places the immediate market order to buy.
        TradeService buyExchangeTradeService = toExchange.getTradeService();
        buyExchangeTradeService.placeMarketOrder(orders[1]);
    }

    public ArrayList<ArbitrageModel> getArbitrageData() {
        ArrayList<ArbitrageModel> ad = arbitrageMapper.getData();
        return ad;
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

    private MarketOrder convertXchangeTrade(String currencyPair, BigDecimal amount)
            throws ExchangeConnectionException {
        switch (currencyPair.toUpperCase()) {
            case "BTCETH": {
                return new MarketOrder(Order.OrderType.BID, amount, new CurrencyPair(Currency.BTC, Currency.ETH));
            }
            case "ETHBTC": {
                return new MarketOrder(Order.OrderType.ASK, amount, new CurrencyPair(Currency.ETH, Currency.BTC));
            }
            case "BTCLTC": {
                return new MarketOrder(Order.OrderType.BID, amount, new CurrencyPair(Currency.BTC, Currency.LTC));
            }
            case "LTCBTC": {
                return new MarketOrder(Order.OrderType.ASK, amount, new CurrencyPair(Currency.LTC, Currency.BTC));
            }
            case "BTCXRP": {
                return new MarketOrder(Order.OrderType.BID, amount, new CurrencyPair(Currency.BTC, Currency.XRP));
            }
            case "XRPBTC": {
                return new MarketOrder(Order.OrderType.ASK, amount, new CurrencyPair(Currency.XRP, Currency.BTC));
            }
            case "BTCBCH": {
                return new MarketOrder(Order.OrderType.BID, amount, new CurrencyPair(Currency.BTC, Currency.BCH));
            }
            case "BCHBTC": {
                return new MarketOrder(Order.OrderType.ASK, amount, new CurrencyPair(Currency.BCH, Currency.BTC));
            }
            default:
                throw new ExchangeConnectionException("Bad currency pair", HttpStatus.BAD_REQUEST);
        }
    }

    private String flipCurrencyPair(String currencyPair) {
     return currencyPair.substring(3) + currencyPair.substring(0,3);
    }
}
