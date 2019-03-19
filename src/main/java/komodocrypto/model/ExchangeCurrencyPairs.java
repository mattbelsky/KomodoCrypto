package komodocrypto.model;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.ArrayList;
import java.util.List;

public class ExchangeCurrencyPairs {

    private Exchange exchange;
    private List<CurrencyPair> currencyPairs;

    public ExchangeCurrencyPairs(Exchange exchange) {
        this.exchange = exchange;
        this.currencyPairs = new ArrayList<>();
    }

    public Exchange getExchange() {
        return exchange;
    }

    public List<CurrencyPair> getCurrencyPairs() {
        return currencyPairs;
    }

    public String getExchangeName() {
        return exchange.getExchangeSpecification().getExchangeName();
    }

    public String[][] getCurrencyPairsArray() {

        int size = currencyPairs.size();
        String[][] cpArray = new String[size][];
        for (int i = 0; i < size; i++) {
            CurrencyPair cp = currencyPairs.get(i);
            cpArray[i][0] = cp.base.getSymbol();
            cpArray[i][1] = cp.counter.getSymbol();
        }
        return cpArray;
    }

    public void addCurrencyPair(CurrencyPair cp) {
        currencyPairs.add(cp);
    }
}
