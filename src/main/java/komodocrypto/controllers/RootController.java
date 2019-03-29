package komodocrypto.controllers;

import komodocrypto.exceptions.custom_exceptions.InsufficientFundsException;
import komodocrypto.model.account.AccountInfoDTO;
import komodocrypto.model.account.BalanceDTO;
import komodocrypto.model.account.WalletDTO;
import komodocrypto.model.arbitrage.ArbitrageModel;
import komodocrypto.model.user.User;
import komodocrypto.services.arbitrage.ArbitrageScanningService;
import komodocrypto.model.*;
import komodocrypto.services.arbitrage.ArbitrageTradeService;
import komodocrypto.services.exchanges.ExchangeService;
import komodocrypto.services.trades.BaseTradeService;
import komodocrypto.services.users.UserService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

import static komodocrypto.security.SecurityConstants.SIGN_UP_URL;

@RestController
public class RootController {

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArbitrageScanningService arbitrageScanningService;

    @Autowired
    @Qualifier("BaseTradeService")
    private BaseTradeService baseTradeService;

    @Autowired
    @Qualifier("ArbitrageTradeService")
    private ArbitrageTradeService arbitrageTradeService;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /********** CREATE/REMOVE USER & MODIFY USER SETTINGS **********/

    @PostMapping(SIGN_UP_URL)
    public RootResponse createUser(@RequestBody User user) {

        boolean userIsUnique = userService.usernameIsUnique(user);
        StringBuilder messageBuilder = new StringBuilder();
        String usernameNotUniqueMsg = "Username must be unique.";

        if (userService.usernameNotNull(user) == false)
            messageBuilder.append("Username is null. ");
        if (userService.passwordNotNull(user) == false)
            messageBuilder.append("Password is empty. ");
        if (userService.emailNotNull(user) == false)
            messageBuilder.append("Email is empty. ");
        else if (userService.validateEmail(user) == false)
            messageBuilder.append("Invalid email.");
        if (userIsUnique == false)
            messageBuilder.append(usernameNotUniqueMsg);
        if (messageBuilder.length() > 0) {
            String message = messageBuilder.toString();
            return new RootResponse(message, null);
        }

        try {
            userService.addUser(user);
        } catch (SQLIntegrityConstraintViolationException e) {
            return new RootResponse(usernameNotUniqueMsg, null);
        }

        User addedUser = (User) userService.loadUserByUsername(user.getUsername());
        return new RootResponse("User successfully registered.", addedUser);
    }

    /********** GET USER ACCOUNT & CURRENCY PAIR DATA FOR EXCHANGES **********/

    @GetMapping("/api/{exchange}/currencypairs")
    public RootResponse getSupportedCurrencyPairs(@PathVariable("exchange") String exchangeName,
                                                  @RequestParam(value = "base", required = false) String base,
                                                  @RequestParam(value = "counter", required = false) String counter) {

        Exchange exchange = exchangeService.createExchange(exchangeName);
        List<String> currencyPair = exchange
                .getExchangeMetaData()
                .getCurrencyPairs()
                .keySet()
                .stream()
                .filter(cp -> {
                    if (base != null && counter == null)
                        return cp.base.getCurrencyCode().equals(base);
                    else if (base == null && counter != null)
                        return cp.counter.getCurrencyCode().equals(counter);
                    else
                        return true; // Returns true by default if both or neither a base & counter are specified -- no use for both.
                })
                .map(cp -> cp.toString())
                .collect(Collectors.toList());
        return new RootResponse("Currency pairs supported for exchange " + exchangeName + ".", currencyPair);
    }

    @GetMapping("/api/{exchange}/account")
    public RootResponse getAccountInfo(@PathVariable("exchange") String exchangeName) throws IOException {

        Exchange exchange = exchangeService.createExchange(exchangeName);
        AccountInfoDTO accountInfoDTO = getAccountInfoDTO(exchange);
        return new RootResponse("Account info for exchange " + exchangeName + ".", accountInfoDTO);
    }

    @GetMapping("/api/{exchange}/wallet")
    public BigDecimal getWalletBalance(@PathVariable("exchange") String exchangeName,
                                       @RequestParam("currency") String currencyName) throws IOException {

        Exchange exchange = exchangeService.createExchange(exchangeName);
        Currency currency = new Currency(currencyName);
        BigDecimal balance = exchange
                .getAccountService()
                .getAccountInfo()
                .getWallet() // The id for the exchange "wallet," which holds all balances, NOT exchange's currency wallet address
                .getBalance(currency)
                .getAvailable();
        return balance;
    }

    @GetMapping("/api/{exchange}/wallet/address")
    public String getWalletAddress(@PathVariable("exchange") String exchangeName,
                              @RequestParam("currency") String currency) {

        String walletAddress = exchangeService.buildGetWalletAddrMethodName(exchangeName, currency);
        System.out.println(walletAddress);
        return walletAddress;
    }

    /********** MAKE TRADES **********/

    @PostMapping("/api/{exchange}/trade/market")
    public RootResponse makeMarketTrade(@PathVariable("exchange") String exchangeName,
                                        @RequestParam("base") String base,
                                        @RequestParam("counter") String counter,
                                        @RequestParam("amount") double amount) throws IOException {

        TradeModel tradeModel = baseTradeService.buildTradeModel(exchangeName, base, counter, amount, Order.OrderType.ASK);
        MarketOrder marketOrder = baseTradeService.buildMarketOrder(tradeModel);
        String transactionId = baseTradeService.makeMarketTrade(tradeModel, marketOrder);
        TradeService tradeService = tradeModel.getExchange().getTradeService();
        List<Order> orders = tradeService.getOrder(transactionId).stream().collect(Collectors.toList());
        String order = orders.get(0).toString();
        return new RootResponse("Order details for order ID " + transactionId + ":", order);
    }

    @PostMapping("/api/{exchange}/withdraw")
    public RootResponse withdrawFunds(@PathVariable("exchange") String fromExchangeName,
                                      @RequestParam("toexchange") String toExchangeName,
                                      @RequestParam("currency") String currencyName,
                                      @RequestParam("amount") double amountDouble) throws IOException {

        Exchange fromExchange = exchangeService.createExchange(fromExchangeName);
        Exchange toExchange = exchangeService.createExchange(toString());
        Currency currency = new Currency(currencyName);
        BigDecimal amount = new BigDecimal(amountDouble);
        String address = exchangeService.getWalletAddress(fromExchangeName, currencyName);
        fromExchange.getAccountService().withdrawFunds(currency, amount, address);
        AccountInfoDTO accountInfoDTO = getAccountInfoDTO(toExchange);
        return new RootResponse("Account info for exchange " + toExchangeName + ".", accountInfoDTO);
    }

    /********** SCAN FOR AND TRADE ARBITRAGE OPPORTUNITIES **********/

    @PostMapping("/api/arbitrage")
    public RootResponse setArbitrageScanningInterval(@RequestParam("milliseconds") int milliseconds) {

        // In case the input is negative
        long interval = Math.abs(milliseconds);

        // Sets the scan/trade class's run() method to execute every specified number of milliseconds after previous
        // completion.
        threadPoolTaskScheduler.scheduleWithFixedDelay(new ScanAndTradeArbitrage(), interval);
        return new RootResponse("Scanning and trading operation commenced. Will continue to scan for " +
                "arbitrage opportunities every " + milliseconds + " milliseconds after previous execution has completed.",
                null);
    }

    @DeleteMapping("/api/arbitrage")
    public RootResponse stopScan() {
        threadPoolTaskScheduler.destroy();
        return new RootResponse("Scanning and trading operation stopped.", null);
    }

    // Runnable inner class is used for scan/trade functionality so that process can be initiated and scheduled when the
    // desired endpoint is hit and easily terminated with ThreadPoolTaskScheduler.
    private class ScanAndTradeArbitrage implements Runnable {

        @Override
        public void run() {

            List<Exchange> exchanges = exchangeService.generateExchangesList();
            try {
                List<ArbitrageModel> arbitrageOpportunities = arbitrageScanningService.getPossibleArbitrageOpportunities(exchanges);
                ArbitrageModel best = arbitrageScanningService.getBestArbitrageOpportunity(arbitrageOpportunities);

                // Sets the amount to trade as the entire balance of the selling wallet.
                BigDecimal amount = getWalletBalance(
                        best.getHighBidExchange().getExchangeSpecification().getExchangeName(),
                        best.getCurrencyPair().base.getCurrencyCode()
                );
                best.setAmount(amount);

                // Builds the market orders and executes the trades.
                MarketOrder[] marketOrders = arbitrageTradeService.buildArbitrageMarketOrders(best);
                arbitrageTradeService.makeArbitrageMarketTrades(best, marketOrders);

            } catch (IOException e) {
                // Nothing.
            } catch (InsufficientFundsException e) {
                // Nothing yet.
            }
        }
    }

    /********** HELPER METHODS **********/

    private AccountInfoDTO getAccountInfoDTO(Exchange exchange) throws IOException {
        // For each AccountInfo, need username, trading fee, wallets
        // For each Wallet, need id, name, balances
        // For each Balance, need total, available, frozen, loaned, borrowed, withdrawing, depositing
        AccountInfo accountInfo = exchange.getAccountService().getAccountInfo();
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
        accountInfoDTO.setUsername(accountInfo.getUsername());
        accountInfoDTO.setTradingFee(accountInfo.getTradingFee());

        List<Wallet> wallets = accountInfo.getWallets().values().stream().collect(Collectors.toList());
        List<WalletDTO> walletDTOs = new ArrayList<>();

        for (Wallet wallet : wallets) {

            WalletDTO walletDTO = new WalletDTO();
            walletDTO.setId(wallet.getId());
            walletDTO.setName(wallet.getName());

            List<Balance> balances = wallet
                    .getBalances()
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            List<String> currencies = wallet
                    .getBalances()
                    .keySet()
                    .stream()
                    .map(currency -> currency.getSymbol().toString())
                    .collect(Collectors.toList());
            HashMap<String, BalanceDTO> balanceDTOs = new HashMap<>();

            for (int i = 0; i < balances.size(); i++) {

                Balance balance = balances.get(i);
                String currency = currencies.get(i);
                BalanceDTO balanceDTO = new BalanceDTO(
                        balance.getTotal(),
                        balance.getAvailable(),
                        balance.getFrozen(),
                        balance.getLoaned(),
                        balance.getBorrowed(),
                        balance.getWithdrawing(),
                        balance.getDepositing()
                );
                balanceDTOs.put(currency, balanceDTO);
            }

            walletDTO.setBalances(balanceDTOs);
            walletDTOs.add(walletDTO);
        }

        accountInfoDTO.setWallets(walletDTOs);
        return accountInfoDTO;
    }
}
