package komodocrypto.model.exchanges;

import java.math.BigDecimal;
import java.util.List;

public class ExchangeModel {

    int id;
    String exchangeName;
    BigDecimal takerTradeFee;
    BigDecimal makerTradeFee;
    List<ExchangeInOutLimits> exchangeLimits;

    public int getId() {
        return id;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public BigDecimal getTakerTradeFee() {
        return takerTradeFee;
    }

    public BigDecimal getMakerTradeFee() {
        return makerTradeFee;
    }

    public List<ExchangeInOutLimits> getExchangeLimits() {
        return exchangeLimits;
    }

    public void setExchangeLimits(List<ExchangeInOutLimits> exchangeLimits) {
        this.exchangeLimits = exchangeLimits;
    }
}
