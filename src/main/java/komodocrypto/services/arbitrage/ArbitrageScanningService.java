package komodocrypto.services.arbitrage;

import komodocrypto.exceptions.custom_exceptions.InsufficientFundsException;
import komodocrypto.exceptions.custom_exceptions.TableEmptyException;
import komodocrypto.mappers.ArbitrageMapper;
import komodocrypto.mappers.database.CurrencyMapper;
import komodocrypto.mappers.exchanges.ExchangeMapper;
import komodocrypto.mappers.exchanges.ExchangeWalletMapper;
import komodocrypto.model.arbitrage.ArbitrageModel;
import komodocrypto.services.exchanges.ExchangeService;
import komodocrypto.services.trades.TradeService;
import org.apache.ibatis.jdbc.Null;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;

@Service
public class ArbitrageScanningService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ExchangeService exchangeService;

    @Autowired
    ExchangeWalletMapper exchangeWalletMapper;

    @Autowired
    ExchangeMapper exchangeMapper;

    @Autowired
    CurrencyMapper currencyMapper;

    @Autowired
    TradeService tradeService;

    @Autowired
    ArbitrageMapper arbitrageMapper;

    @Autowired
    CoinSpreadTracking coinSpreadTracking;

    // Cycles through each currency pair one second after the previous execution has finished and finds the two most
    // ideal exchanges for an arbitrage trade.
    // For testing purposes, this method also indicates how long the scanning service takes to complete.
//    @Scheduled(fixedDelay = 5000)
    public void scanExchangesByPair() throws Exception {

        ArrayList<CurrencyPair> pairsList = exchangeService.generateCurrencyPairList();

        for (CurrencyPair cp : pairsList) {

            String pairString = cp.toString();
            logger.info("Starting arbitrage service for pair " + pairString + ".");
            long startTimeArbitrageScanner = System.currentTimeMillis();

            // Gets the two exchanges that offer the best arbitrage opportunity for this currency pair.
            // Objects in array will be null if no exchanges support this pair.
            Exchange[] exchanges = getBestArbitrageExchangesForPair(cp);
            if (exchanges[0] == null) continue;

            // Get timestamp, pair, difference, low ask, high bid
            long endTimeArbitrageScanner = System.currentTimeMillis();
            Timestamp timestamp = new Timestamp(endTimeArbitrageScanner);
            long timeElapsedArbitrageScanner = endTimeArbitrageScanner - startTimeArbitrageScanner;
            logger.info("Arbitrage opportunity for pair " + pairString + " identified at " + timestamp.toString() + ".");
            logger.info("Arbitrage service for pair " + pairString + " took " + timeElapsedArbitrageScanner +
                    " ms to complete.");

            // Checks the exchange wallet's balance. If <= 0 or null, logs the exception and forces an early iteration
            // of the loop.
            try {
                if (exchangeWalletMapper.getBalanceByExchangeIdAndCurrencyId(
                        exchangeMapper.getExchangeIdByName(
                                exchanges[0].getExchangeSpecification().getExchangeName()),
                        currencyMapper.getIdBySymbol(pairString.substring(0, pairString.indexOf("/"))))
                        .compareTo(BigDecimal.valueOf(0)) == 0)
                    throw new InsufficientFundsException("", HttpStatus.OK);
            } catch (NullPointerException | InsufficientFundsException e) {
                logger.error("Insufficient balance in exchange wallet to make an arbitrage trade.");
                continue;
            }

            // Creates and begins initializing the values of the TradeData object containing the data to make a trade
            // and persist the data.
            tradeService.buildTradeModel(exchanges, cp);

            // Executes the mock trade.
            String fromCurrency = pairString.substring(0, pairString.indexOf("/"));
            tradeService.executeTrade();

            logger.info("Finishing arbitrage service for pair " + pairString + ".");
        }
    }

    /**
     * Scans a given list of exchanges and returns an array of two exchanges for the given currency pair.
     * [0] is the highest priced exchange, while [1] is the lowest.
     * @param currencyPair the crypto currency pair to be scanned
     * @throws IOException
     */
//    @Async
    public Exchange[] getBestArbitrageExchangesForPair(CurrencyPair currencyPair) throws IOException, TableEmptyException {

        // An array that will contain the highest and lowest price for a given coin
        // [0] is for the highest, [1] for the lowest
        Exchange[] exchangeArray = new Exchange[2];

        // TODO See if this can be executed and cached on startup, as this is a time waster here.
        ArrayList<Exchange> exchangesList = exchangeService.generateDefaultExchangeList();

        MarketDataService marketDataService;
        Ticker currentTicker = null;
        BigDecimal lowestAsk = null;
        BigDecimal highestBid = null;
        String cp = currencyPair.toString();

        for (Exchange ex : exchangesList) {

            //If the exchange doesn't support the given pair, skip it.
            if (!doesExchangeSupportCurrency(ex, currencyPair)) continue;

            logger.info("Scanning " + ex.getExchangeSpecification().getExchangeName()
                    + " for " + cp + " . . . ");

            //Get the given market last price for the given pair
            marketDataService = ex.getMarketDataService();
            currentTicker = marketDataService.getTicker(currencyPair);
            logger.info("Bid|Ask: " + currentTicker.getBid() + " - " + currentTicker.getAsk());

            //If it's the first exchange to compare...
            if (exchangeArray[0] == null && exchangeArray[1] == null) {

                exchangeArray[0] = ex;
                exchangeArray[1] = ex;
                highestBid  = currentTicker.getBid();
                lowestAsk   = currentTicker.getAsk();

            }
            //Compare to the highest and lowest priced exchange and replace them if necessary
            else if (currentTicker.getBid().compareTo(highestBid) == 1){

                highestBid = currentTicker.getBid();
                exchangeArray[0] = ex;

            } else if (currentTicker.getAsk().compareTo(lowestAsk) == -1){

                lowestAsk = currentTicker.getAsk();
                exchangeArray[1] = ex;
            }
        }

        // Throws an exception if each element of the exchange array is empty before continuing to execute.
        if (exchangeArray[0] == null && exchangeArray[1] == null) {

            String message = "No exchange supports the trading pair " + cp;
            logger.error(message);
            throw new TableEmptyException(message, HttpStatus.NO_CONTENT);
        }

        // Creates arbitrage model object and persists arbitrage data into the database.
        // POSSIBLE TIME WASTING BOTTLENECK HERE!

        // Gets the timestamp from the ticker. If the ticker contains no timestamp, uses the current time.
        Timestamp ts = new Timestamp(System.currentTimeMillis());

        try {
            ts.setTime(currentTicker.getTimestamp().getTime());
        } catch (NullPointerException e) {
            logger.error("Current ticker contains a null timestamp. Current system time used instead.");
        }

        ArbitrageModel arbitrageData = new ArbitrageModel();
        arbitrageData.setTimestamp(ts);
        arbitrageData.setCurrencyPair(cp);
        arbitrageData.setDifference(highestBid.subtract(lowestAsk));
        arbitrageData.setHighBid(highestBid);
        arbitrageData.setLowAsk(lowestAsk);
        arbitrageData.setHighBidExchange(exchangeArray[0].getExchangeSpecification().getExchangeName());
        arbitrageData.setLowAskExchange(exchangeArray[1].getExchangeSpecification().getExchangeName());
        arbitrageMapper.addArbitrageData(arbitrageData);

        logger.info("Highest priced bid -> " + exchangeArray[0].getExchangeSpecification().getExchangeName()
                + ": " + exchangeArray[0].getMarketDataService().getTicker(currencyPair).getBid());
        logger.info("Lowest priced ask -> " + exchangeArray[1].getExchangeSpecification().getExchangeName()
                + ": " + exchangeArray[0].getMarketDataService().getTicker(currencyPair).getAsk());

        return exchangeArray;
    }

    /**
     * Returns a boolean value indicating if the given exchange supports the given currency pair.
     * @param ex - An exchange object
     * @param currencyPair - A currency pair object
     * @return true if the currency pair is supported, false if it is not
     */

    public boolean doesExchangeSupportCurrency(Exchange ex, CurrencyPair currencyPair){
        if(ex.getExchangeMetaData().getCurrencyPairs().containsKey(currencyPair))
            return true;
        else
            return false;
    }

    public String[] getExchangeNames(CurrencyPair currencyPair) throws IOException, TableEmptyException {

        Exchange[] exchanges = getBestArbitrageExchangesForPair(currencyPair);
        String[] exchangeNames = new String[2];
        exchangeNames[0] = exchanges[0].getExchangeSpecification().getExchangeName();
        exchangeNames[1] = exchanges[1].getExchangeSpecification().getExchangeName();

        return exchangeNames;
    }

//    @Async
//    public Ticker getTickerBinance(String currencyPair){
//        BinanceTicker binanceTicker = new BinanceTicker();
//        Ticker ticker = null;
//
//        try {
//            ticker = binanceTicker.getTickerInfo(currencyPair);
//        } catch (IOException e) {
//            System.out.println("Unable to get ticker info for " + currencyPair.toString() + " from Bittrex");
//            e.printStackTrace();
//        }
//        return ticker;
//    }
//
//    /**
//     * Queries the Bittrex exchange for the lastest data for the given currency pair
//     * @param currencyPair a Currency pair OBJ
//     * @return a Ticker obj containing the price info of the currency pair
//     */
//    @Async
//    public Ticker getTickerBittrex(CurrencyPair currencyPair){
//        BittrexTicker bittrexTicker = new BittrexTicker();
//        Ticker ticker = null;
//        try {
//            ticker = bittrexTicker.getCurrencyPairTicker(currencyPair);
//        } catch (IOException e) {
//            System.out.println("Unable to get ticker info for " + currencyPair.toString() + " from Bittrex");
//            e.printStackTrace();
//        }
//        return ticker;
//    }
//
//    /**
//     * Queries the Bitstamp exchange for the lastest data for the given currency pair
//     * @param currencyPair a Currency pair OBJ
//     * @return a Ticker obj containing the price info of the currency pair
//     */
//    @Async
//    public Ticker getTickerBitstamp(CurrencyPair currencyPair){
//        BitstampTicker bitstampTicker = new BitstampTicker();
//        Ticker ticker = null;
//        try {
//            ticker = bitstampTicker.getTickerInfo(currencyPair);
//        } catch (ExchangeConnectionException e) {
//            System.out.println("Exception while connecting to Bitstamp exchange");
//            e.printStackTrace();
//        }
//        return ticker;
//    }
}
