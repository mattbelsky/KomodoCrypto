package komodocrypto.model.account;

import java.math.BigDecimal;

public class BalanceDTO {

    private BigDecimal total;
    private BigDecimal available;
    private BigDecimal frozen;
    private BigDecimal loaned;
    private BigDecimal borrowed;
    private BigDecimal withdrawing;
    private BigDecimal depositing;

    public BalanceDTO(BigDecimal total, BigDecimal available, BigDecimal frozen, BigDecimal loaned, BigDecimal borrowed,
                      BigDecimal withdrawing, BigDecimal depositing) {
        this.total = total;
        this.available = available;
        this.frozen = frozen;
        this.loaned = loaned;
        this.borrowed = borrowed;
        this.withdrawing = withdrawing;
        this.depositing = depositing;
    }

    public BalanceDTO() {
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public BigDecimal getFrozen() {
        return frozen;
    }

    public void setFrozen(BigDecimal frozen) {
        this.frozen = frozen;
    }

    public BigDecimal getLoaned() {
        return loaned;
    }

    public void setLoaned(BigDecimal loaned) {
        this.loaned = loaned;
    }

    public BigDecimal getBorrowed() {
        return borrowed;
    }

    public void setBorrowed(BigDecimal borrowed) {
        this.borrowed = borrowed;
    }

    public BigDecimal getWithdrawing() {
        return withdrawing;
    }

    public void setWithdrawing(BigDecimal withdrawing) {
        this.withdrawing = withdrawing;
    }

    public BigDecimal getDepositing() {
        return depositing;
    }

    public void setDepositing(BigDecimal depositing) {
        this.depositing = depositing;
    }
}
