package komodocrypto.model.database;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class GroupPortfolio {

    int id;
    BigDecimal depositValue;
    BigDecimal currentValue;
    int numInvestors;
    Timestamp timestamp;

    public int getId() {
        return id;
    }

    public BigDecimal getDepositValue() {
        return depositValue;
    }

    public void setDepositValue(BigDecimal depositValue) {
        this.depositValue = depositValue;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public int getNumInvestors() {
        return numInvestors;
    }

    public void setNumInvestors(int numInvestors) {
        this.numInvestors = numInvestors;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
