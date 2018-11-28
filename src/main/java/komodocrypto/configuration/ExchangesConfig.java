package komodocrypto.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExchangesConfig {

    //------BINANCE------//

    @Value("${binance.apiKey}")
    private String binanceApiKey;

    @Value("${binance.secretKey}")
    private String binanceSecretKey;

    @Value("${binance.wallet.btc.id}")
    private String binanceBTCWalletId;

    @Value("${binance.wallet.eth.id}")
    private String binanceETHWalletId;

    @Value("${binance.wallet.bch.id}")
    private String binanceBCHWalletId;

    @Value("${binance.wallet.ltc.id}")
    private String binanceLTCWalletId;

    @Value("${binance.wallet.xrp.id}")
    private String binanceXRPWalletId;

    @Value("${binance.wallet.xrp.tag}")
    private String binanceXRPWalletTag;

    //------BITTREX------//

    @Value("${bittrex.username}")
    private String bittrexUsername;

    @Value("${bittrex.apiKey}")
    private String bittrexApiKey;

    @Value("${bittrex.secretKey}")
    private String bittrexSecretKey;

    @Value("${bittrex.wallet.btc.id}")
    private String bittrexBTCWalletId;

    @Value("${bittrex.wallet.eth.id}")
    private String bittrexETHWalletId;

    @Value("${bittrex.wallet.bch.id}")
    private String bittrexBCHWalletId;

    @Value("${bittrex.wallet.ltc.id}")
    private String bittrexLTCWalletId;

    @Value("${bittrex.wallet.xrp.id}")
    private String bittrexXRPWalletId;

    //------COINBASE PRO------//

    @Value("${coinbasepro.apiKey}")
    private String coinbaseproApiKey;

    @Value("${coinbasepro.secretKey}")
    private String coinbaseproSecretKey;

    @Value("${coinbasepro.passphrase}")
    private String coinbaseproPassphrase;

    @Value("${coinbasepro.wallet.btc.id}")
    private String coinbaseproBTCWalletId;

    @Value("${coinbasepro.wallet.eth.id}")
    private String coinbaseproETHWalletId;

    @Value("${coinbasepro.wallet.bch.id}")
    private String coinbaseproBCHWalletId;

    @Value("${coinbasepro.wallet.ltc.id}")
    private String coinbaseproLTCWalletId;

    @Value("${coinbasepro.wallet.usdc.id}")
    private String coinbaseproUSDCWalletId;

    //------KRAKEN-------//

    @Value("${kraken.apiKey}")
    private String krakenApiKey;

    @Value("${kraken.privateKey}")
    private String krakenPrivateKey;

    @Value("${kraken.wallet.btc.id}")
    private String krakenBTCWalletId;

    @Value("${kraken.wallet.eth.id}")
    private String krakenETHWalletId;

    @Value("${kraken.wallet.bch.id}")
    private String krakenBCHWalletId;

    @Value("${kraken.wallet.ltc.id}")
    private String krakenLTCWalletId;

    @Value("${kraken.wallet.xrp.id}")
    private String krakenXRPWalletId;

    @Value("${kraken.wallet.xrp.tag}")
    private String krakenXRPWalletTag;

    //------BLOCKCHAIN WALLET------//

    @Value("${blockchainwallet.btc.id}")
    private String blockchainWalletBTCId;

    // Getters

    public String getBinanceApiKey() {
        return binanceApiKey;
    }

    public String getBinanceSecretKey() {
        return binanceSecretKey;
    }

    public String getBinanceBTCWalletId() {
        return binanceBTCWalletId;
    }

    public String getBinanceETHWalletId() {
        return binanceETHWalletId;
    }

    public String getBinanceBCHWalletId() {
        return binanceBCHWalletId;
    }

    public String getBinanceLTCWalletId() {
        return binanceLTCWalletId;
    }

    public String getBinanceXRPWalletId() {
        return binanceXRPWalletId;
    }

    public String getBinanceXRPWalletTag() {
        return binanceXRPWalletTag;
    }

    public String getBittrexUsername() {
        return bittrexUsername;
    }

    public String getBittrexApiKey() {
        return bittrexApiKey;
    }

    public String getBittrexSecretKey() {
        return bittrexSecretKey;
    }

    public String getBittrexBTCWalletId() {
        return bittrexBTCWalletId;
    }

    public String getBittrexETHWalletId() {
        return bittrexETHWalletId;
    }

    public String getBittrexBCHWalletId() {
        return bittrexBCHWalletId;
    }

    public String getBittrexLTCWalletId() {
        return bittrexLTCWalletId;
    }

    public String getBittrexXRPWalletId() {
        return bittrexXRPWalletId;
    }

    public String getCoinbaseproApiKey() {
        return coinbaseproApiKey;
    }

    public String getCoinbaseproSecretKey() {
        return coinbaseproSecretKey;
    }

    public String getCoinbaseproPassphrase() {
        return coinbaseproPassphrase;
    }

    public String getCoinbaseproBTCWalletId() {
        return coinbaseproBTCWalletId;
    }

    public String getCoinbaseproETHWalletId() {
        return coinbaseproETHWalletId;
    }

    public String getCoinbaseproBCHWalletId() {
        return coinbaseproBCHWalletId;
    }

    public String getCoinbaseproLTCWalletId() {
        return coinbaseproLTCWalletId;
    }

    public String getCoinbaseproUSDCWalletId() {
        return coinbaseproUSDCWalletId;
    }

    public String getKrakenApiKey() {
        return krakenApiKey;
    }

    public String getKrakenPrivateKey() {
        return krakenPrivateKey;
    }

    public String getKrakenBTCWalletId() {
        return krakenBTCWalletId;
    }

    public String getKrakenETHWalletId() {
        return krakenETHWalletId;
    }

    public String getKrakenBCHWalletId() {
        return krakenBCHWalletId;
    }

    public String getKrakenLTCWalletId() {
        return krakenLTCWalletId;
    }

    public String getKrakenXRPWalletId() {
        return krakenXRPWalletId;
    }

    public String getKrakenXRPWalletTag() {
        return krakenXRPWalletTag;
    }

    public String getBlockchainWalletBTCId() {
        return blockchainWalletBTCId;
    }
}
