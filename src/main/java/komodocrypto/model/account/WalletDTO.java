package komodocrypto.model.account;

import java.util.HashMap;

public class WalletDTO {

    String id;
    String name;
    HashMap<String, BalanceDTO> balances;

    public WalletDTO(String id, String name, HashMap<String, BalanceDTO> balances) {
        this.id = id;
        this.name = name;
        this.balances = balances;
    }

    public WalletDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, BalanceDTO> getBalances() {
        return balances;
    }

    public void setBalances(HashMap<String, BalanceDTO> balances) {
        this.balances = balances;
    }
}
