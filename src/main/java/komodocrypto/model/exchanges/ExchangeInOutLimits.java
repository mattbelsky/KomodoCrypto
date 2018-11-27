package komodocrypto.model.exchanges;

import java.math.BigDecimal;

public class ExchangeInOutLimits {

    int id;
    String currency;
    BigDecimal depositMin;
    BigDecimal withdrawalMin;
    BigDecimal withdrawalMax;
    String withdrawalMaxPeriod;
    String withdrawalCurrencyEquivalent;

    public int getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getDepositMin() {
        return depositMin;
    }

    public BigDecimal getWithdrawalMin() {
        return withdrawalMin;
    }

    public BigDecimal getWithdrawalMax() {
        return withdrawalMax;
    }

    public String getWithdrawalMaxPeriod() {
        return withdrawalMaxPeriod;
    }

    public String getWithdrawalCurrencyEquivalent() {
        return withdrawalCurrencyEquivalent;
    }
}
