package komodocrypto.services.trades;

import komodocrypto.configuration.ExchangesConfig;
import komodocrypto.exceptions.custom_exceptions.InsufficientFundsException;
import komodocrypto.mappers.database.CurrencyMapper;
import komodocrypto.mappers.database.CurrencyPairsMapper;
import komodocrypto.mappers.database.TransactionMapper;
import komodocrypto.mappers.exchanges.ExchangeMapper;
import komodocrypto.mappers.exchanges.ExchangeWalletMapper;
import komodocrypto.model.TradeData;
import komodocrypto.model.TradeModel;
import komodocrypto.model.database.Transaction;
import komodocrypto.model.exchanges.ExchangeInOutLimits;
import komodocrypto.model.exchanges.ExchangeModel;
import komodocrypto.model.exchanges.ExchangeWallet;
import komodocrypto.services.exchanges.ExchangeService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.MarketOrder;
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

@Service("BaseTradeService")
public class BaseTradeService {

    /*  This class should serve as an intermediary between the arbitrage scanning service and the asset tracking
        service. When an arbitrage opportunity is identified and persisted, the methods in this class should
        1)  Call exchange wallet mapper to see if selling currency is in selling wallet
            a) If not, break from execution.
        2)  If so, call asset tracking service to...
            a) Create record of transaction
            b) Remove all of the currency to sell out of the selling wallet
            c) Add it to the buying wallet

     */

    Logger logger = LoggerFactory.getLogger(this.getClass());

    // This value is multiplied by any trading fees in case the client's wallet has an insufficient balance due to
    // miscalculating the fees.
    private final BigDecimal SAFETY_BUFFER = new BigDecimal(1.5);

    @Autowired
    ExchangeMapper exchangeMapper;

    @Autowired
    ExchangesConfig exchangesConfig;

    @Autowired
    ExchangeService exchangeService;

    /*
        Calculate fees
        add to price
        query wallet via Xchange to ensure funds exist
        create transaction object
        make trade
        query to ensure trade is successful
        query wallet to see actual balance
        persist transaction object
        call asset tracking service to update balances etc.
        • Methods
        ◦ BigDecimal calculateFees(Exchange exchange, BigDecimal amount)
        ◦ boolean hasSufficientFunds(String walletId)
        ◦ MarketOrder buildMarketOrder(Exchange exchange, CurrencyPair cp, BigDecimal amount)
        ◦ void makeMarketTrade(MarketOrder) – trades at current market rates
            ▪ if tradeSuccessful() == true
                • query API for account info, get info,
        ◦ boolean tradeSuccessful()
        ◦ BigDecimal getUserWalletBalance()
        ◦ String getExchangeWalletId(Exchange exchange)
     */
    public BigDecimal calculateTradingFees(Exchange exchange, CurrencyPair cp, BigDecimal amount) {

        String exchangeName = exchange.getExchangeSpecification().getExchangeName();
        String currencyCode = cp.base.getCurrencyCode();

        ExchangeModel exchangeModel = exchangeMapper.getExchangeModelByName(exchangeName);
        List<ExchangeInOutLimits> exchangeLimits = new ArrayList<ExchangeInOutLimits>();
        exchangeLimits.add(exchangeMapper.getLimitsbyExchangeAndCurrency(exchangeName, currencyCode));
        exchangeModel.setExchangeLimits(exchangeLimits);

        BigDecimal fee = exchangeModel.getTakerTradeFee().multiply(amount);
        return fee;
    }

    public boolean hasSufficientFunds(BigDecimal amount, BigDecimal balance, BigDecimal fees) {

        BigDecimal totalTradeCost = amount.add(fees).add(SAFETY_BUFFER);
        return balance.compareTo(totalTradeCost) == 1;
    }

    public MarketOrder buildMarketOrder(TradeModel tradeModel)
            throws IOException {

        /*  Get base currency
            Get exchange
            Get exchange's base currency wallet id
            Get wallet balance
            Get trading fee
            If trading fee > wallet balance
                throw exception
            Create market order(BID, amount, currency pair)
        */

        Exchange exchange = tradeModel.getExchange();
        Currency base = tradeModel.getCurrencyPair().base;
        CurrencyPair currencyPair = tradeModel.getCurrencyPair();
        BigDecimal amount = tradeModel.getAmount();
        BigDecimal tradingFee = calculateTradingFees(exchange, currencyPair, amount);
        Order.OrderType orderType = tradeModel.getOrderType();
        String walletId = tradeModel.getWalletId();
        BigDecimal balance = getClientWalletBalance(exchange, base, walletId);

        if (!hasSufficientFunds(amount, balance, tradingFee)) return null;

        return new MarketOrder(orderType, amount, currencyPair);
    }

    public void makeMarketTrade(TradeModel tradeModel, MarketOrder order) throws IOException {

        Exchange exchange = tradeModel.getExchange();
        exchange.getTradeService().placeMarketOrder(order);
    }

    public boolean wasTradeSuccessful() {
        return false;
    }

    public BigDecimal getClientWalletBalance(Exchange exchange, Currency currency, String walletId) throws IOException {

        return exchange.getAccountService()
                .getAccountInfo()
                .getWallet(walletId)
                .getBalance(currency)
                .getAvailable();
    }

    public TradeModel buildTradeModel(String exchangeName, String base, String counter, double amountDouble, String orderType)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // The object to return
        TradeModel tradeModel = new TradeModel();

        Exchange exchange = exchangeService.createExchange(exchangeName);
        CurrencyPair currencyPair = new CurrencyPair(base, counter);
        BigDecimal amount = new BigDecimal(amountDouble);

        String getWalletIdMethodName = exchangeService.buildGetExchangeCurrencyWalletIdMethodName(exchangeName, base);
        Method getWalletId = exchangesConfig.getClass().getMethod(getWalletIdMethodName, null);
        String walletId = getWalletId.invoke(exchangesConfig, null).toString();

        if (orderType.toLowerCase().equals("sell")) tradeModel.setOrderType(Order.OrderType.BID);
        else if (orderType.toLowerCase().equals("buy")) tradeModel.setOrderType(Order.OrderType.ASK);
        else return null;

        // Builds the trade model.
        tradeModel.setCurrencyPair(currencyPair);
        tradeModel.setExchange(exchange);
        tradeModel.setAmount(amount);
        tradeModel.setWalletId(walletId);

        return tradeModel;
    }

//    // Builds a TradeData object, containing the information needed to execute a trade and persist its data.
//    // NOTE: May need to overload this method or something when doing manual trades.
//    // TODO Look into how to loosely couple the TradeData object with the classes where it's used.
//    public void buildTradeModel(Exchange[] exchanges, CurrencyPair currencyPair) throws InsufficientFundsException {
//
//        logger.info("Building trade model...");
//
//        tradeData.setFromExchange(exchanges[0]);
//        tradeData.setToExchange(exchanges[1]);
//        tradeData.setCurrencyPair(currencyPair);
//
//        int idFromExchange = exchangeMapper.getExchangeIdByName(exchanges[0].getExchangeSpecification().getExchangeName());
//        int idToExchange = exchangeMapper.getExchangeIdByName(exchanges[1].getExchangeSpecification().getExchangeName());
//        tradeData.setIdFromExchange(idFromExchange);
//        tradeData.setIdToExchange(idToExchange);
//
//        String cpString = currencyPair.toString();
//        int idFromCurrency = currencyMapper.getIdBySymbol(cpString.substring(0, cpString.indexOf("/")));
//        int idToCurrency = currencyMapper.getIdBySymbol(cpString.substring(cpString.indexOf("/") + 1));
//        tradeData.setIdFromCurrency(idFromCurrency);
//        tradeData.setIdToCurrency(idToCurrency);
//        tradeData.setIdCurrencyPair(currencyPairsMapper.getCurrencyPairId(idFromCurrency, idToCurrency));
//
//        // Throws an exception for now if there is no record of this currency pair in the exchange wallet or if the
//        // available balance is 0.
//        // TODO Find a better way to handle this.
//        try {
//
//            BigDecimal balanceFromExchange = exchangeWalletMapper.getBalanceByExchangeIdAndCurrencyId(idFromExchange, idFromCurrency);
//            tradeData.setBalanceFromExchange(balanceFromExchange);
//
//            if (balanceFromExchange.compareTo(BigDecimal.valueOf(0)) <= 0) throw new Exception();
//
//        } catch (Exception e) {
//
//            throw new InsufficientFundsException("Insufficient funds to trade in exchange " +
//                    exchanges[0].getExchangeSpecification().getExchangeName() + ".", HttpStatus.OK);
//        }
//
//        try {
//
//            BigDecimal balanceToExchange = exchangeWalletMapper.getBalanceByExchangeIdAndCurrencyId(idToExchange, idToCurrency);
//            tradeData.setBalanceToExchange(balanceToExchange);
//
//            if (balanceToExchange.compareTo(BigDecimal.valueOf(0)) <= 0) throw new Exception();
//
//        } catch (Exception e) {
//
//            throw new InsufficientFundsException("Insufficient funds to trade in exchange " +
//                    exchanges[1].getExchangeSpecification().getExchangeName() + ".", HttpStatus.OK);
//        }
//
//        logger.info("Finished building trade model.");
//    }

    // Executes the trade.
    public void makeMarketTrade() {



//        logger.info("Executing trade...");
//
//        /*  Will eventually make actual trades with exchanges and confirm that the data used to updated the database is
//            accurate, but for now we'll just update the tables ourselves. */
//
//        // Creates new transaction logs reflecting the trade.
//        Transaction transactionFrom = new Transaction();
//        transactionFrom.setExchange_id(tradeData.getIdFromExchange());
//        transactionFrom.setCurrency_pair_id(tradeData.getIdCurrencyPair());
//        transactionFrom.setTransaction_type("sell");
//        BigDecimal balanceFrom = tradeData.getBalanceFromExchange();
//        transactionFrom.setTransaction_amount(balanceFrom);
//        transactionFrom.setBalance_before_transaction(balanceFrom);
//        transactionFrom.setAlgorithm("arbitrage");
//
//        Transaction transactionTo = new Transaction();
//        transactionTo.setExchange_id(tradeData.getIdToExchange());
//        transactionTo.setCurrency_pair_id(tradeData.getIdCurrencyPair());
//        transactionTo.setTransaction_type("buy");
//        BigDecimal balanceTo = tradeData.getBalanceToExchange();
//        transactionTo.setTransaction_amount(balanceFrom);
//        transactionTo.setBalance_before_transaction(balanceTo);
//        transactionTo.setAlgorithm("arbitrage");
//
//        // Adds the new entries to the transaction table.
//        transactionMapper.addTransaction(transactionFrom);
//        transactionMapper.addTransaction(transactionTo);
//
//        // Creates and initializes the values in the POJO used to update the exchange wallet table.
//        ExchangeWallet fromWallet = new ExchangeWallet();
//        fromWallet.setExchangeId(tradeData.getIdFromExchange());
//        fromWallet.setCurrencyId(tradeData.getIdFromCurrency());
//        fromWallet.setAvailable(BigDecimal.valueOf(0));
//
//        ExchangeWallet toWallet = new ExchangeWallet();
//        toWallet.setExchangeId(tradeData.getIdToExchange());
//        toWallet.setCurrencyId(tradeData.getIdToCurrency());
//        toWallet.setAvailable(BigDecimal.valueOf(0));
//
//        // Adds new entries to the exchange wallet table reflecting the trade.
//        exchangeWalletMapper.insertNewData(fromWallet);
//        exchangeWalletMapper.insertNewData(toWallet);
//
//        // TODO Deal with client & group portfolios -- By what criteria do we update the balance?
//
//        logger.info("Finished executing the trade.");
    }
}
