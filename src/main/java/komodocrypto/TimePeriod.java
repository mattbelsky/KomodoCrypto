package komodocrypto;

public enum TimePeriod {

    HOURS_IN_DAY(24), MIN_IN_HOUR(60), SEC_IN_MIN(60),
    SEC_IN_HOUR(MIN_IN_HOUR.value * SEC_IN_MIN.value),
    SEC_IN_DAY(HOURS_IN_DAY.value * SEC_IN_HOUR.value),
    SEC_IN_WEEK(SEC_IN_DAY.value * 7);

    int value;

    TimePeriod(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
