package komodocrypto.model.account;

import java.math.BigDecimal;
import java.util.List;

public class AccountInfoDTO {

    private String username;
    private BigDecimal tradingFee;
    private List<WalletDTO> wallets;

    public AccountInfoDTO(String username, BigDecimal tradingFee, List<WalletDTO> wallets) {
        this.username = username;
        this.tradingFee = tradingFee;
        this.wallets = wallets;
    }

    public AccountInfoDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getTradingFee() {
        return tradingFee;
    }

    public void setTradingFee(BigDecimal tradingFee) {
        this.tradingFee = tradingFee;
    }

    public List<WalletDTO> getWallets() {
        return wallets;
    }

    public void setWallets(List<WalletDTO> wallets) {
        this.wallets = wallets;
    }
}
