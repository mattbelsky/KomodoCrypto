package komodocrypto.services.exchanges;

import komodocrypto.utils.ExchangesUtil;
import komodocrypto.mappers.CurrencyPairsMapper;
import komodocrypto.mappers.ExchangeMapper;
import komodocrypto.model.database.CurrencyPairs;
import komodocrypto.model.exchanges.ExchangeModel;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.KrakenExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExchangeService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ExchangesUtil exchangesUtil;

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

    public String formatExchangeName(String name) {

        switch (name.toLowerCase()) {
            case "binance":
                return "Binance";
            case "bittrex":
                return "Bittrex";
            case "kraken":
                return "Kraken";
            case "coinbasepro":
                return "CoinbasePro";
            default:
                return null;
        }
    }

    public Exchange createExchange(String exchangeName) {

        ExchangeSpecification exSpec;
        String formattedName = formatExchangeName(exchangeName);

        switch (formattedName) {

            case "Binance":
                exSpec = new BinanceExchange().getDefaultExchangeSpecification();
                exSpec.setApiKey(exchangesUtil.getBinanceApiKey());
                exSpec.setSecretKey(exchangesUtil.getBinanceSecretKey());
                break;

            case "Bittrex":
                exSpec = new BittrexExchange().getDefaultExchangeSpecification();
                exSpec.setUserName(exchangesUtil.getBittrexUsername());
                exSpec.setApiKey(exchangesUtil.getBittrexApiKey());
                exSpec.setSecretKey(exchangesUtil.getBittrexSecretKey());
                break;

            case "Kraken":
                exSpec = new KrakenExchange().getDefaultExchangeSpecification();
                exSpec.setApiKey(exchangesUtil.getKrakenApiKey());
                exSpec.setSecretKey(exchangesUtil.getKrakenPrivateKey());
                break;

            case "CoinbasePro":
                exSpec = new CoinbaseProExchange().getDefaultExchangeSpecification();
                exSpec.setApiKey(exchangesUtil.getCoinbaseProApiKey());
                exSpec.setSecretKey(exchangesUtil.getCoinbaseProSecretKey());
                exSpec.setExchangeSpecificParametersItem("passphrase", exchangesUtil.getCoinbaseProPassphrase());
                break;

            default:
                String message = exchangeName + " is not supported by this application.";
                logger.error(message);
                return null;
        }

        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(exSpec);
        return exchange;
    }

    // Gets the wallet address for the specified currency and exchange by building the name of and invoking the necessary
    // method in the configuration class using reflection.
    public String getWalletAddress(String exchange, String currency) {

        exchange = formatExchangeName(exchange);
        try {
            String getWalletAddrMethodName = buildGetWalletAddrMethodName(exchange, currency);
            Method getWalletAddr = exchangesUtil.getClass().getMethod(getWalletAddrMethodName, null);
            String address = getWalletAddr.invoke(exchangesUtil, null).toString();
            return address;
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            // TODO Figure out how to handle exceptions thrown by this method.
            return null;
        }
    }

    public String buildGetWalletAddrMethodName(String exchange, String currency) {

        String getWalletAddrMethodName = "get"
                + exchange
                + currency.toUpperCase()
                + "WalletId";
        return getWalletAddrMethodName;
    }

    public List<String> getCurrencyPairsByExchange(Exchange exchange) {
        return exchange.getExchangeMetaData().getCurrencyPairs().keySet().stream().map(cp -> cp.toString()).collect(Collectors.toList());
    }

}
