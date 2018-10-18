package komodocrypto.services.exchanges.generic;

import komodocrypto.configuration.exchange_utils.BitstampUtil;
import komodocrypto.exceptions.custom_exceptions.ExchangeConnectionException;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.service.account.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class KomodoAccountInfo {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    BitstampUtil bitstampUtil;

    public AccountService selectExchange(String exchange) throws ExchangeConnectionException {
        switch (exchange.toLowerCase()) {
            case "bitstamp" : {
                // Create ExchangeData
                Exchange bitstamp = bitstampUtil.createExchange();
                // Connect to account
                AccountService accountService = bitstamp.getAccountService();
                return accountService;
            }
            case "binance" : {

            }

            case "bittrex" : {

            }
            default:
                throw new ExchangeConnectionException("Invalid ExchangeData", HttpStatus.BAD_REQUEST);
        }
    }
}
