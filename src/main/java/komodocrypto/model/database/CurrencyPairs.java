package komodocrypto.model.database;

public class CurrencyPairs {

    int id;
    int currencyIdBase;
    int currencyIdCounter;
    String currencySymbolBase;
    String currencySymbolCounter;

    public int id() {
        return id;
    }

    public int getCurrencyIdBase() {
        return currencyIdBase;
    }

    public void setCurrencyIdBase(int currencyIdBase) {
        this.currencyIdBase = currencyIdBase;
    }

    public int getCurrencyIdCounter() {
        return currencyIdCounter;
    }

    public void setCurrencyIdCounter(int currencyIdCounter) {
        this.currencyIdCounter = currencyIdCounter;
    }

    public String getCurrencySymbolBase() {
        return currencySymbolBase;
    }

    public void setCurrencySymbolBase(String currencySymbolBase) {
        this.currencySymbolBase = currencySymbolBase;
    }

    public String getCurrencySymbolCounter() {
        return currencySymbolCounter;
    }

    public void setCurrencySymbolCounter(String currencySymbolCounter) {
        this.currencySymbolCounter = currencySymbolCounter;
    }
}
