package komodocrypto.controllers;

import komodocrypto.model.RootResponse;
import komodocrypto.model.TradeModel;
import komodocrypto.services.exchanges.ExchangeService;
import komodocrypto.services.trades.BaseTradeService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ExchangeController {

    @Autowired
    ExchangeService exchangeService;

    @Autowired
    @Qualifier("BaseTradeService")
    BaseTradeService baseTradeService;

    @GetMapping("/{exchange}/account")
    public String getAccountService(@PathVariable("exchange") String exchangeName) throws IOException {

        Exchange exchange;
        switch (exchangeName) {

            case "binance":
                exchange = exchangeService.createExchange("Binance");
                break;

            case "bittrex":
                exchange = exchangeService.createExchange("Bittrex");
                break;

            case "coinbasepro":
                exchange = exchangeService.createExchange("CoinbasePro");
                break;

            case "kraken":
                exchange = exchangeService.createExchange("Kraken");
                break;

            default:
                return "Exchange not supported.";
        }

        return exchange.getAccountService().getAccountInfo().toString();
    }

    @GetMapping("/ticker/{exchange}")
    public List<Ticker> getTicker(@PathVariable("exchange") String exchangeName,
                            @RequestParam(value = "base", required = false) String base,
                            @RequestParam(value = "counter", required = false) String counter) throws IOException {

        MarketDataService marketDataService = exchangeService.createExchange(exchangeName).getMarketDataService();

        if (base == null || counter == null)
            return marketDataService.getTickers(null);
        else {
            ArrayList<Ticker> tickers = new ArrayList<>();
            tickers.add(marketDataService.getTicker(new CurrencyPair(base, counter)));
            return tickers;
        }
    }

    @PostMapping("/trade/market")
    public RootResponse makeMarketTrade(@RequestParam("exchange") String exchange,
                                        @RequestParam("base") String base,
                                        @RequestParam("counter") String counter,
                                        @RequestParam("amount") double amount,
                                        @RequestParam("ordertype") String orderType)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {

        TradeModel tradeModel = baseTradeService.buildTradeModel(exchange, base, counter, amount, orderType);
        MarketOrder marketOrder = baseTradeService.buildMarketOrder(tradeModel);
        baseTradeService.makeMarketTrade(tradeModel, marketOrder);

        return new RootResponse(HttpStatus.OK, "Market order successfully placed.", null);
    }

    @PostMapping("/transfer")
    public RootResponse transferFunds(@RequestParam("fromexchange") String fromExchange,
                                      @RequestParam("toexchange") String toExchange,
                                      @RequestParam("currency") String currency,
                                      @RequestParam("amount") double amount) {

        return null;
    }

    //-----------FOR TESTING PURPOSES------------//

    @GetMapping("/{exchange}/currencypairs")
    public HashMap<String, List<String>> getSupportedCurrencyPairs(@PathVariable("exchange") String exchangeName) {

        HashMap<String, List<String>> currencyPairs = new HashMap<>();

        if (exchangeName.equals("all")) {

            List<Exchange> exchanges = exchangeService.generateExchangesList();

            for (Exchange e : exchanges) {

                String name = e.getExchangeSpecification().getExchangeName();
                List<String> pairs = getCurrencyPairByExchange(e);
                currencyPairs.put(name, pairs);
            }

        } else {

            Exchange e = exchangeService.createExchange(exchangeName);
            String name = e.getExchangeSpecification().getExchangeName();
            List<String> pairs = getCurrencyPairByExchange(e);
            currencyPairs.put(name, pairs);
        }

        return currencyPairs;
    }

    private List<String> getCurrencyPairByExchange(Exchange exchange) {

        Map<CurrencyPair, CurrencyPairMetaData> cpMeta = exchange.getExchangeMetaData().getCurrencyPairs();
        List<String> currencyPair = cpMeta
                .keySet()
                .stream()
                .map(cp -> cp.toString())
                .collect(Collectors.toList());
        return currencyPair;
    }

//
//
//    //=========================== Binance exchange ==============================
//    @Autowired
//    BinanceAccount binanceAccount;
//
//    @Autowired
//    BinanceTicker binanceTicker;
//
//    @Autowired
//    BinanceTradeImpl binanceTrade;
//
//
//    @Autowired
//    BitstampAccount bitstampAccount;
//
//    @Autowired
//    BitstampTicker bitstampTicker;
//
//    @Autowired
//    BitstampTradeImpl bitstampTradeImpl;
//
//
//    /**
//     * Binance: [GET] Return account info
//     *
//     * @return JSON of Account object
//     */
//    @GetMapping("/binance/account")
//    public Account getBinanceAccountInfo() {
//        return binanceAccount.getAccountInfo();
//    }
//
//    /**
//     * Binance: [GET] Return deposit address for rebalancing
//     *
//     * @param asset String asset you want to deposit
//     * @return JSON of DepositAddress object
//     */
//    @GetMapping("/binance/deposit")
//    public DepositAddress getBinanceDepositInfo(@RequestParam(value = "asset") String asset) {
//        return binanceAccount.getDepositAddress(asset);
//    }
//
//    /**
//     * Binance: [GET] Return latest ticker info for a specific asset pair on Binance
//     *
//     * @param pair String trading pair, e.g. ETHBTC, LTCBTC etc.
//     * @return TickerPrice of the latest prices (JSON)
//     */
//    @GetMapping("/binance/ticker")
//    public TickerPrice getBinanceTickerInfo(@RequestParam(value = "pair") String pair) {
//        return binanceTicker.getTickerInfo(pair);
//    }
//
//    /**
//     * Binance: [GET] Return latest ticker info for all trading pairs on Binance
//     *
//     * @return List of all the latest prices (JSON)
//     */
//    @GetMapping("/binance/ticker/")
//    public List<TickerPrice> getAllBinanceTickerInfo() {
//        return binanceTicker.getAllTickerInfo();
//    }
//
//    /**
//     * Binance: [POST] Withdraw funds
//     *
//     * @param asset   String asset symbol, case sensitive (e.g. ETH, BTC, LTC etc.)
//     * @param address String wallet address to withdraw money into
//     * @param amount  String amount of asset to be withdrawn
//     * @return WithdrawResult. If no withdrawl, returns nothing
//     */
//    @PostMapping("/binance/withdraw")
//    public WithdrawResult makeBinanceWithdrawl(@RequestParam(value = "asset") String asset,
//                                               @RequestParam(value = "address") String address,
//                                               @RequestParam(value = "amount") String amount) {
//        return binanceAccount.makeWithdrawl(asset, address, amount);
//    }
//
//    /**
//     * Binance: [POST] Test market trade
//     *
//     * @param pair   String asset pair, case & order sensitive (e.g. LTCBTC etc.)
//     * @param amount String amount of asset to be traded
//     */
//    @PostMapping("/binance/tradetest")
//    public void makeBinanceTradeTest(@RequestParam(value = "pair") String pair,
//                                     @RequestParam(value = "amount") String amount) {
//        binanceTrade.testMarketOrder(pair, amount);
//    }
//
//    /**
//     * Binance: [POST] Live market trade
//     *
//     * @param pair   String asset pair, case & order sensitive (e.g. ETHBTC, BTCETH, LTCBTC etc.)
//     * @param amount String amount of asset to be traded
//     */
//    @PostMapping("/binance/trade")
//    public void makeBinanceTrade(@RequestParam(value = "pair") String pair,
//                                 @RequestParam(value = "amount") String amount) {
//        binanceTrade.placeMarketOrder(pair, amount);
//    }
//
//    /**
//     * Binance: [DELETE] Cancel trade order
//     *
//     * @param pair    String asset pair, case & order sensitive (e.g. ETHBTC, BTCETH, LTCBTC etc.)
//     * @param orderId Long id of order to be cancelled
//     */
//    @DeleteMapping("/binance/trade")
//    public void cancelBinanceTrade(@RequestParam(value = "pair") String pair,
//                                   @RequestParam(value = "id") Long orderId) {
//        binanceTrade.cancelOrder(pair, orderId);
//    }

//    @GetMapping("/binance/backfill")
//    public List<Candlestick> getHistoricalCandlestick(@RequestParam(value = "pair") String pair) {
//        return binanceTicker.getHistorical(pair);
    }

//    //=========================== Bitstamp exchange ==============================
//
//    /**
//     * Bitstamp: [GET] Account information from Bitstamp
//     *
//     * @return Account balance for a given asset in Bitstamp
//     */
//    @GetMapping("/bitstamp/balance")
//    public BitstampBalance getBitstampBalance(@RequestParam(value = "asset") Currency asset) throws ExchangeConnectionException {
//        return bitstampAccount.getBalance(asset);
//    }
//
//    /**
//     * Bitstamp: [GET] Trade history from Bitstamp
//     *
//     * @return AccountInfo object (JSON)
//     */
//    @GetMapping("/bitstamp/mytrades")
//    public List<FundingRecord> getBitstampTradeHistory() throws ExchangeConnectionException {
//        return bitstampAccount.getTradeHistory();
//    }
//
//
//    /**
//     * Bitstamp: [GET] Return deposit address for rebalancing
//     *
//     * @param asset Currency asset you want to deposit
//     * @return String of deposit address for the given asset
//     */
//    @GetMapping("/bitstamp/deposit")
//    public String getBitstampDepositInfo(@RequestParam(value = "asset") Currency asset)
//            throws ExchangeConnectionException {
//        return bitstampAccount.getDepositAddress(asset);
//    }
//
//    /**
//     * Bitstamp: [POST] Withdraw funds
//     *
//     * @param asset   Currency asset symbol, case sensitive (e.g. ETH, BTC, LTC etc.)
//     * @param amount  BigDecimal amount of asset to be withdrawn
//     * @param address String wallet address to withdraw money into
//     * @return WithdrawResult. If no withdrawl, returns nothing
//     */
//    @PostMapping("/bitstamp/withdraw")
//    public String makeBitstampWithdrawl(@RequestParam(value = "asset") Currency asset,
//                                        @RequestParam(value = "address") String address,
//                                        @RequestParam(value = "amount") BigDecimal amount)
//            throws ExchangeConnectionException {
//        return bitstampAccount.makeWithdrawl(asset, amount, address);
//    }
//
//    /**
//     * Bitstamp: [GET] Return latest ticker info for a specific asset pair on Bitstamp
//     *
//     * @param pair CurrencyPair asset symbol requested (ETH_BTC, XRP_BTC, LTC_BTC, BCH_BTC ONLY)
//     * @return Ticker object of the latest prices (JSON)
//     */
//    @GetMapping("/bitstamp/ticker")
//    public Ticker getBitstampTickerInfo(@RequestParam(value = "pair") CurrencyPair pair)
//            throws ExchangeConnectionException {
//        return bitstampTicker.getTickerInfo(pair);
//    }
//
//    /**
//     * Bitstamp: [POST] Place market order
//     *
//     * @param type   OrderType (BID or ASK ONLY)
//     * @param amount BigDecimal amount of asset to be traded
//     * @param pair   CurrencyPair asset symbol to be traded (ETH_BTC, XRP_BTC, LTC_BTC, BCH_BTC ONLY)
//     * @return String or market order return value (needed to attempt cancellation)
//     */
//    @PostMapping("/bitstamp/market")
//    public String makeBitstampMarketOrder(@RequestParam(value = "type") Order.OrderType type,
//                                          @RequestParam(value = "amount") BigDecimal amount,
//                                          @RequestParam(value = "pair") CurrencyPair pair)
//            throws ExchangeConnectionException {
//        return bitstampTradeImpl.placeMarketOrder(type, amount, pair);
//    }
//
//    /**
//     * Bitstamp: [DELETE] Cancel market order
//     *
//     * @param marketOrderReturnValue String identifying order to be cancelled,
//     *                               returned at time order is placed
//     * @return boolean if order was cancelled or not
//     */
//    @DeleteMapping("/bitstamp/market")
//    public Boolean cancelBitstampMarketOrder(@RequestParam(value = "id") String marketOrderReturnValue)
//            throws ExchangeConnectionException {
//        return bitstampTradeImpl.cancelMarketOrder(marketOrderReturnValue);
//    }
//
//    /**
//     * Bitstamp: [GET] Get open order list from Bitstamp
//     *
//     * @return OpenOrders object (JSON)
//     */
//    @GetMapping("/bitstamp/orders")
//    public OpenOrders getBitstampOpenOrders()
//            throws ExchangeConnectionException {
//        return bitstampTradeImpl.getOpenOrders();
//    }
//
//    //=========================== Bittrex exchange ==============================
//
//    @Autowired
//    BittrexAccount bittrexAccount;
//
//    @Autowired
//    BittrexTicker bittrexTicker;
//
//    @Autowired
//    BittrexTradeImpl bittrexTrade;
//
//    /**
//     * Bittrex: [GET] Account information from Bittrex
//     *
//     * @return Account balance for a given asset in Bittrex
//     */
//    @GetMapping("/bittrex/balance")
//    public Balance getBittrexBalance(@RequestParam(value = "asset") Currency asset) throws ExchangeConnectionException {
//        return bittrexAccount.getCurrencyBalance(asset);
//    }
//
//    /**
//     * Bittrex: [GET] Return deposit address for rebalancing
//     *
//     * @param asset Currency asset you want to deposit
//     * @return String of deposit address for the given asset
//     */
//    @GetMapping("/bittrex/deposit")
//    public String getBittrexDepositInfo(@RequestParam(value = "asset") Currency asset)
//            throws ExchangeConnectionException {
//        return bittrexAccount.getDepositAddress(asset);
//    }
//
//}
