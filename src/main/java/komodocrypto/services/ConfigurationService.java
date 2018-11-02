package komodocrypto.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {

    @Value("${binance.apiKey}")
    private String binanceApiKey;

    @Value("${binance.secretKey}")
    private String binanceSecretKey;

    @Value("${bittrex.username}")
    private String bittrexUsername;

    @Value("${bittrex.apiKey}")
    private String bittrexApiKey;

    @Value("${bittrex.secretKey}")
    private String bittrexSecretKey;

    @Value("${coinbase.apiKey}")
    private String coinbaseApiKey;

    @Value("${coinbase.secretKey}")
    private String coinbaseSecretKey;

    public String getBinanceApiKey() {
        return binanceApiKey;
    }

    public String getBinanceSecretKey() {
        return binanceSecretKey;
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

    public String getCoinbaseApiKey() {
        return coinbaseApiKey;
    }

    public String getCoinbaseSecretKey() {
        return coinbaseSecretKey;
    }
}
