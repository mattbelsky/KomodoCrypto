package komodocrypto.services.exchanges;

import komodocrypto.mappers.database.CurrencyPairsMapper;
import komodocrypto.mappers.exchanges.ExchangeMapper;
import komodocrypto.model.database.CurrencyPairs;
import komodocrypto.model.exchanges.ExchangeData;
import komodocrypto.model.exchanges.ExchangeNames;
import komodocrypto.services.ConfigurationService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.coinbase.v2.CoinbaseExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.service.account.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExchangeService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    ExchangeMapper exchangeMapper;

    @Autowired
    CurrencyPairsMapper currencyPairsMapper;

    // Generates a list of exchanges from the database that the application is using.
    @Cacheable("ExchangesList")
    public ArrayList<Exchange> generateDefaultExchangeList() {

        // The list of ExchangeData objects to return
        ArrayList<Exchange> exchangesList = new ArrayList<>();

        // The list of exchangeData from the database
        List<ExchangeData> exchangeData = exchangeMapper.getExchanges();

        // Tries to create a new ExchangeData object and add it to the list to be returned for each exchange found in the
        // database.
        for (ExchangeData e : exchangeData) {

            Exchange exchange;
            String exchangeName = e.getExchangeName();

            // Use the factory to get exchange API using default settings.
            if (exchangeName.equals(ExchangeNames.Binance.toString()))
                exchange = ExchangeFactory.INSTANCE.createExchange(BinanceExchange.class.getName());
            else if (exchangeName.equals(ExchangeNames.Bitstamp.toString()))
                exchange = ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName());
            else if (exchangeName.equals(ExchangeNames.Bittrex.toString()))
                exchange = ExchangeFactory.INSTANCE.createExchange(BittrexExchange.class.getName());
            else if (exchangeName.equals(ExchangeNames.Kraken.toString()))
                exchange = ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName());
            else if (exchangeName.equals(ExchangeNames.Coinbase.toString()))
                exchange = ExchangeFactory.INSTANCE.createExchange(CoinbaseExchange.class.getName());
            else exchange = null;

            if (exchange != null) exchangesList.add(exchange);
        }

        return exchangesList;
    }

    // Generates a list of currency pairs from the database that the application is using.
    @Cacheable("CurrencyPairs")
    public ArrayList<CurrencyPair> generateCurrencyPairList() {

        // The list of CurrencyPair objects to return
        ArrayList<CurrencyPair> pairsList = new ArrayList<>();

        // The list of pairs in the database
        List<CurrencyPairs> currencyPairs = currencyPairsMapper.getAllCurrencyPairs();

        // Creates and adds a new CurrencyPair object to the list to be returned.
        for (CurrencyPairs cp : currencyPairs) {

            String base = cp.getSymbol1();
            String counter = cp.getSymbol2();
            CurrencyPair currencyPair = new CurrencyPair(base, counter);
            pairsList.add(currencyPair);
        }

        return pairsList;
    }

    public Exchange createExchange(String exchangeName) {

        // Checks if the specified exchange is supported by this application.
        // TODO Handle this better.
        ArrayList<String> exchangeNamesList = exchangeMapper.getExchangeNames();
        if (!exchangeNamesList.contains(exchangeName)) {
            logger.error(exchangeName + " is not supported by this application.");
            return null;
        }

        ExchangeSpecification exSpec;

        switch (exchangeName.toLowerCase()) {

            case "binance":
                exSpec = new BinanceExchange().getDefaultExchangeSpecification();
                exSpec.setApiKey(configurationService.getBinanceApiKey());
                exSpec.setSecretKey(configurationService.getBinanceSecretKey());
                break;

            case "bittrex":
                exSpec = new BittrexExchange().getDefaultExchangeSpecification();
                exSpec.setUserName(configurationService.getBittrexUsername());
                exSpec.setApiKey(configurationService.getBittrexApiKey());
                exSpec.setSecretKey(configurationService.getBittrexSecretKey());
                break;

            // Default is Coinbase.
            default:
                exSpec = new CoinbaseExchange().getDefaultExchangeSpecification();
                exSpec.setApiKey(configurationService.getCoinbaseApiKey());
                exSpec.setSecretKey(configurationService.getCoinbaseSecretKey());
                break;
        }

        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(exSpec);
        return exchange;
    }

    public String getAccountInfo(Exchange exchange) throws IOException {

        AccountService accountService = exchange.getAccountService();
        AccountInfo accountInfo = accountService.getAccountInfo();
        return accountInfo.toString();
    }
}
