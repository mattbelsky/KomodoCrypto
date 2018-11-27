package komodocrypto.services.exchanges;

import komodocrypto.configuration.ExchangesConfig;
import komodocrypto.mappers.database.CurrencyPairsMapper;
import komodocrypto.mappers.exchanges.ExchangeMapper;
import komodocrypto.model.database.CurrencyPairs;
import komodocrypto.model.exchanges.ExchangeModel;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExchangeService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ExchangesConfig exchangesConfig;

    @Autowired
    ExchangeMapper exchangeMapper;

    @Autowired
    CurrencyPairsMapper currencyPairsMapper;

    // Generates a list of exchanges from the database that the application is using.
    @Cacheable("ExchangesList")
    public List<Exchange> generateExchangesList() {

        // The list of ExchangeData objects to return
        List<Exchange> exchangesList = new ArrayList<>();

        // The list of exchangeData from the database
        List<ExchangeModel> exchangeModels = exchangeMapper.getExchanges();

        // Creates a new Exchange object based on the name of each supported exchange from the database and adds it to
        // the list.
        for (ExchangeModel em : exchangeModels) {

            String name = em.getExchangeName();
            Exchange exchange = createExchange(name);
            exchangesList.add(exchange);
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

            String base = cp.getCurrencySymbolBase();
            String counter = cp.getCurrencySymbolCounter();
            CurrencyPair currencyPair = new CurrencyPair(base, counter);
            pairsList.add(currencyPair);
        }

        return pairsList;
    }

    public Exchange createExchange(String exchangeName) {

        ExchangeSpecification exSpec;

        switch (exchangeName.toLowerCase()) {

            case "binance":
                exSpec = new BinanceExchange().getDefaultExchangeSpecification();
                exSpec.setApiKey(exchangesConfig.getBinanceApiKey());
                exSpec.setSecretKey(exchangesConfig.getBinanceSecretKey());
                break;

            case "bittrex":
                exSpec = new BittrexExchange().getDefaultExchangeSpecification();
                exSpec.setUserName(exchangesConfig.getBittrexUsername());
                exSpec.setApiKey(exchangesConfig.getBittrexApiKey());
                exSpec.setSecretKey(exchangesConfig.getBittrexSecretKey());
                break;

            case "kraken":
                exSpec = new KrakenExchange().getDefaultExchangeSpecification();
                exSpec.setApiKey(exchangesConfig.getKrakenApiKey());
                exSpec.setSecretKey(exchangesConfig.getKrakenPrivateKey());
                break;

            case "coinbasepro":
                exSpec = new CoinbaseProExchange().getDefaultExchangeSpecification();
                exSpec.setApiKey(exchangesConfig.getCoinbaseproApiKey());
                exSpec.setSecretKey(exchangesConfig.getCoinbaseproSecretKey());
                // TODO Find the right password.
                exSpec.setExchangeSpecificParametersItem("passphrase", exchangesConfig.getCoinbaseproPassword());
                break;

            default:
                String message = exchangeName + " is not supported by this application.";
                logger.error(message);
                return null;
        }

        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(exSpec);
        return exchange;
    }

    public void transferFunds(String fromExchange, String toExchange, String currencyName, double amountDouble)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {

        // String withdrawFunds(Currency currency, BigDecimal amount, String address)
        String getWalletIdMethodName = buildGetExchangeCurrencyWalletIdMethodName(toExchange, currencyName);
        Method getWalletId = exchangesConfig.getClass().getMethod(getWalletIdMethodName, null);
        String walletId = getWalletId.invoke(exchangesConfig, null).toString();

        Currency currency = new Currency(currencyName);
        BigDecimal amount = new BigDecimal(amountDouble);
        Exchange exchange = createExchange(fromExchange);

        exchange.getAccountService().withdrawFunds(currency, amount, walletId);
    }

    public String buildGetExchangeCurrencyWalletIdMethodName(String exchange, String currency) {

        String getWalletIdMethodName = "get"
                + exchange
                + currency.toUpperCase()
                + "WalletId";
        return getWalletIdMethodName;
    }

    // TODO This can go soon.
    public String getAccountInfo(Exchange exchange) throws IOException {

        AccountService accountService = exchange.getAccountService();
        AccountInfo accountInfo = accountService.getAccountInfo();
        return accountInfo.toString();
    }

    // TODO This method may not be necessary.
    public HashMap<String, List<String>> getSupportedCurrencyPairs(Exchange exchange) {

        HashMap<String, List<String>> currencyPairs = new HashMap<>();
        String name = exchange.getExchangeSpecification().getExchangeName();
        List<String> pairs = getCurrencyPairsByExchange(exchange);
        currencyPairs.put(name, pairs);
        return currencyPairs;
    }

    public List<String> getCurrencyPairsByExchange(Exchange exchange) {

        Map<CurrencyPair, CurrencyPairMetaData> cpMeta = exchange.getExchangeMetaData().getCurrencyPairs();
        List<String> currencyPair = cpMeta
                .keySet()
                .stream()
                .map(cp -> cp.toString())
                .collect(Collectors.toList());
        return currencyPair;
    }

}
