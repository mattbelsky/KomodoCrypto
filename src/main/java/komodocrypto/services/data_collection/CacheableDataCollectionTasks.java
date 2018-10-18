package komodocrypto.services.data_collection;

import komodocrypto.mappers.CryptoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheableDataCollectionTasks {

    @Autowired
    CryptoMapper cryptoMapper;

    /**
     * Gets a list of currency pairs that are traded. Each currency is traded against the value of bitcoin, therefore a
     * sample element of the list is { "LTC", "BTC" }.
     * @return the list of pairs
     */
    @Cacheable("TradingPairs")
    public String[][] getTradingPairs() {

        String[] currencies = cryptoMapper.getCurrencies();
        String bitcoin = "BTC";
        String[][] tradingPairs = new String[currencies.length][2];
        int counter = 0;

        for (String currency : currencies) {

            if (currency.equals(bitcoin)) continue;

            tradingPairs[counter][0] = currency;
            tradingPairs[counter][1] = bitcoin;

            counter++;
        }

        return tradingPairs;
    }

    /**
     * Gets a list of exchanges traded upon.
     * @return the list of exchanges
     */
    @Cacheable("Exchanges")
    public String[] getExchanges() {
        return cryptoMapper.getExchanges();
    }
}
