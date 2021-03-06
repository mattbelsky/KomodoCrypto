package komodocrypto.model.exchanges;

import java.math.BigDecimal;

public class ExchangeWallet {

    int exchangeWalletId;
    int exchangeId;
    int currencyId;
    int ClientPortfolioId;
    String depositAddress;
    BigDecimal total;
    BigDecimal available;
    BigDecimal frozen;
    BigDecimal borrowed;
    BigDecimal loaned;
    BigDecimal withdrawing;
    BigDecimal depositing;

    public int getExchangeWalletId() {
        return exchangeWalletId;
    }

    public int getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(int exchangeId) {
        this.exchangeId = exchangeId;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public int getClientPortfolioId() {
        return ClientPortfolioId;
    }

    public void setClientPortfolioId(int clientPortfolioId) {
        ClientPortfolioId = clientPortfolioId;
    }

    public String getDepositAddress() {
        return depositAddress;
    }

    public void setDepositAddress(String depositAddress) {
        this.depositAddress = depositAddress;
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

    public BigDecimal getBorrowed() {
        return borrowed;
    }

    public void setBorrowed(BigDecimal borrowed) {
        this.borrowed = borrowed;
    }

    public BigDecimal getLoaned() {
        return loaned;
    }

    public void setLoaned(BigDecimal loaned) {
        this.loaned = loaned;
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
