package komodocrypto.services.arbitrage;

import komodocrypto.model.arbitrage.ArbitrageModel;
import komodocrypto.services.exchanges.ExchangeService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArbitrageScanningService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ExchangeService exchangeService;

    // Cycles through each currency pair one second after the previous execution has finished and finds the two most
    // ideal exchanges for an arbitrage trade.
    // For testing purposes, this method also indicates how long the scanning service takes to complete.
//    @Scheduled(fixedDelay = 5000)
//    public void scanExchangesByPair() throws Exception {
//
//        ArrayList<CurrencyPair> pairsList = exchangeService.generateCurrencyPairList();
//
//        for (CurrencyPair cp : pairsList) {
//
//            String pairString = cp.toString();
//            logger.info("Starting arbitrage service for pair " + pairString + ".");
//            long startTimeArbitrageScanner = System.currentTimeMillis();
//
//            // Gets the two exchanges that offer the best arbitrage opportunity for this currency pair.
//            // Objects in array will be null if no exchanges support this pair.
//            Exchange[] exchanges = getBestArbitrageExchangesForPair(cp);
//            if (exchanges[0] == null) continue;
//
//            // Get timestamp, pair, difference, low ask, high bid
//            long endTimeArbitrageScanner = System.currentTimeMillis();
//            Timestamp timestamp = new Timestamp(endTimeArbitrageScanner);
//            long timeElapsedArbitrageScanner = endTimeArbitrageScanner - startTimeArbitrageScanner;
//            logger.info("Arbitrage opportunity for pair " + pairString + " identified at " + timestamp.toString() + ".");
//            logger.info("Arbitrage service for pair " + pairString + " took " + timeElapsedArbitrageScanner +
//                    " ms to complete.");
//
//            // Checks the exchange wallet's balance. If <= 0 or null, logs the exception and forces an early iteration
//            // of the loop.
//            try {
//                if (exchangeWalletMapper.getBalanceByExchangeIdAndCurrencyId(
//                        exchangeMapper.getExchangeIdByName(
//                                exchanges[0].getExchangeSpecification().getExchangeName()),
//                        currencyMapper.getIdBySymbol(pairString.substring(0, pairString.indexOf("/"))))
//                        .compareTo(BigDecimal.valueOf(0)) == 0)
//                    throw new InsufficientFundsException("", HttpStatus.OK);
//            } catch (NullPointerException | InsufficientFundsException e) {
//                logger.error("Insufficient balance in exchange wallet to make an arbitrage trade.");
//                continue;
//            }
//
//            // Creates and begins initializing the values of the TradeData object containing the data to make a trade
//            // and persist the data.
//            tradeService.buildTradeModel(exchanges, cp);
//
//            // Executes the mock trade.
//            String fromCurrency = pairString.substring(0, pairString.indexOf("/"));
//            tradeService.executeTrade();
//
//            logger.info("Finishing arbitrage service for pair " + pairString + ".");
//        }
//    }

    /**
     * Scans a given list of exchanges and returns an array of two exchanges for the given currency pair.
     * [0] is the highest priced exchange, while [1] is the lowest.
     * @param currencyPair the crypto currency pair to be scanned
     * @throws Exception
     */
    public ArbitrageModel getBestArbitrageOpportunitiesForPair(List<Exchange> exchanges, CurrencyPair currencyPair) throws IOException {

        // The exchanges to sell and buy from
        // [0] is the exchange to sell from, [1] is the one to buy from
        Exchange[] exchangeArray = new Exchange[2];

        Ticker ticker = null;
        BigDecimal lowestAsk = null;
        BigDecimal highestBid = null;
        String cp = currencyPair.toString();

        for (Exchange e : exchanges) {

            // If the exchange doesn't support the given pair, skip it.
            if (!doesExchangeSupportCurrencyPair(e, currencyPair)) continue;

            logger.info("Scanning "
                    + e.getExchangeSpecification().getExchangeName()
                    + " for "
                    + cp
                    + " . . . ");

            // Get the given market last price for the given pair.
            ticker = e.getMarketDataService().getTicker(currencyPair);
            logger.info("Bid|Ask: "
                    + ticker.getBid()
                    + " - "
                    + ticker.getAsk());

            // If it's the first exchange to compare...
            if (exchangeArray[0] == null && exchangeArray[1] == null) {

                exchangeArray[0] = e;
                exchangeArray[1] = e;
                highestBid = ticker.getBid();
                lowestAsk = ticker.getAsk();

            }
            // Compare to the highest and lowest priced exchange and replace them if necessary.
            else if (ticker.getBid().compareTo(highestBid) == 1) {

                highestBid = ticker.getBid();
                exchangeArray[0] = e;

            } else if (ticker.getAsk().compareTo(lowestAsk) == -1) {

                lowestAsk = ticker.getAsk();
                exchangeArray[1] = e;
            }
        }

        // Returns null if each element of the exchange array is empty before continuing to execute.
        if (exchangeArray[0] == null && exchangeArray[1] == null) {

            String message = "No exchange supports the trading pair " + cp;
            logger.warn(message);
            return null;
        }

        // Returns null if no arbitrage opportunities are available between exchanges.
        if (exchangeArray[0].getExchangeSpecification().getExchangeName().equals(
                exchangeArray[1].getExchangeSpecification().getExchangeName())
                || lowestAsk.compareTo(highestBid) == 1) {

            String message = "No arbitrage opportunities currently available for the trading pair " + cp;
            logger.warn(message);
            return null;
        }

        // Gets the timestamp from the ticker. If the ticker contains no timestamp, uses the current time.
        Timestamp ts = new Timestamp(System.currentTimeMillis());

        try {
            ts.setTime(ticker.getTimestamp().getTime());
        } catch (NullPointerException e) {
            logger.error("Current ticker contains a null timestamp. Current system time used instead.");
        }

        // Builds the arbitrage model.
        ArbitrageModel arbitrageModel = new ArbitrageModel.Builder()
                .timestamp(ts)
                .highBid(highestBid)
                .lowAsk(lowestAsk)
                .difference()
                .currencyPair(currencyPair)
                .highBidExchange(exchangeArray[0])
                .lowAskExchange(exchangeArray[1])
                .build();

//        arbitrageMapper.addArbitrageData(arbitrageData);

        logger.info("Highest priced bid -> " + exchangeArray[0].getExchangeSpecification().getExchangeName()
                + ": " + exchangeArray[0].getMarketDataService().getTicker(currencyPair).getBid());
        logger.info("Lowest priced ask -> " + exchangeArray[1].getExchangeSpecification().getExchangeName()
                + ": " + exchangeArray[0].getMarketDataService().getTicker(currencyPair).getAsk());

        return arbitrageModel;
    }

    public List<ArbitrageModel> getBestArbitrageOpportunitiesForAllCurrencies(List<Exchange> exchanges) throws Exception {

        List<ArbitrageModel> arbitrageOpportunities = new ArrayList<>();
        List<CurrencyPair> currencyPairs = exchangeService.generateCurrencyPairList();

        for (CurrencyPair cp : currencyPairs) {

            ArbitrageModel arbitrageModel = getBestArbitrageOpportunitiesForPair(exchanges, cp);
            if (arbitrageModel == null)
                continue;
            arbitrageOpportunities.add(arbitrageModel);
        }

        return arbitrageOpportunities;
    }

    // Gets a list of all the possible best arbitrage opportunities where the user has funds of the base currency in the
    // current exchange's wallet.
    public List<ArbitrageModel> getPossibleArbitrageOpportunities(List<Exchange> exchanges) throws IOException {

        List<ArbitrageModel> arbitrageOpportunities = new ArrayList<>();
        List<CurrencyPair> currencyPairs = exchangeService.generateCurrencyPairList();

        for (CurrencyPair cp : currencyPairs) {

            ArbitrageModel arbitrageModel = getBestArbitrageOpportunitiesForPair(exchanges, cp);
            // If there are no arbitrage opportunities, continue.
            if (arbitrageModel == null)
                continue;

            for (Exchange exchange : exchanges) {
                if (exchange.getAccountService().getAccountInfo().getWallet().getBalance(cp.base).getAvailable()
                        .equals(BigDecimal.ZERO))
                    continue;
            }
            arbitrageOpportunities.add(arbitrageModel);
        }
        return arbitrageOpportunities;
    }

    // Gets from a list the arbitrage opportunity with the biggest difference between the high bid and low ask.
    public ArbitrageModel getBestArbitrageOpportunity(List<ArbitrageModel> arbitrageModels) {

        ArbitrageModel best = null;
        BigDecimal maxDifference = new BigDecimal(Integer.MIN_VALUE);

        for (ArbitrageModel am : arbitrageModels) {
            BigDecimal difference = am.getDifference();
            if (difference.compareTo(maxDifference) == 1) {
                maxDifference = difference;
                best = am;
            }
        }
        return best;
    }

    /**
     * Returns a boolean value indicating if the given exchange supports the given currency pair.
     * @param ex - An exchange object
     * @param currencyPair - A currency pair object
     * @return true if the currency pair is supported, false if it is not
     */
    public boolean doesExchangeSupportCurrencyPair(Exchange ex, CurrencyPair currencyPair) {

        if (ex.getExchangeMetaData().getCurrencyPairs().containsKey(currencyPair))
            return true;
        else
            return false;
    }

    // Determines whether the each potential arbitrage exchange is profitable in that the amount gained by the difference
    // is the same or greater than the desired profit margin.
    // TODO See how fees factor into the filter calculation.
    public List<ArbitrageModel> getProfitableArbitrages(List<ArbitrageModel> models, BigDecimal amount, BigDecimal margin) {

        if (models.size() == 0)
            return null;
        List<ArbitrageModel> profitable = models
                .stream()
                .filter(m -> amount
                        .multiply(m.getDifference().abs())
                        .compareTo(amount.multiply(margin))
                    >= 0)
                .collect(Collectors.toList());
        return profitable;
    }

//    /**
//     * Gets the names of the exchanges that offer the best arbitrage opportunity for the given currency pair.
//     * @param currencyPair -- the currency pair to query for best arbitrage opportunity
//     * @return the names of the exchanges in string form
//     * @throws IOException
//     * @throws TableEmptyException
//     */
//    public String[] getExchangeNames(CurrencyPair currencyPair) throws IOException, Exception {
//
//        ArbitrageModel arbitrageModel = getBestArbitrageExchangesForPair(currencyPair);
//        String[] exchangeNames = new String[2];
//        exchangeNames[0] = arbitrageModel.getHighBidExchangeName();
//        exchangeNames[1] = arbitrageModel.getLowAskExchangeName();
//
//        return exchangeNames;
//    }



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
