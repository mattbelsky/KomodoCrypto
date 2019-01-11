package komodocrypto.controllers.api;

import komodocrypto.exceptions.custom_exceptions.TableEmptyException;
import komodocrypto.mappers.database.ClientPortfolioMapper;
import komodocrypto.mappers.database.GroupPortfolioMapper;
import komodocrypto.model.RootResponse;
import komodocrypto.services.arbitrage.ArbitrageScanningService;
import komodocrypto.services.exchanges.ExchangeService;
import komodocrypto.services.trades.BaseTradeService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
public class ArbitrageController {

    @Autowired
    ClientPortfolioMapper clientPortfolioMapper;

    @Autowired
    GroupPortfolioMapper groupPortfolioMapper;

    @Autowired
    ExchangeService exchangeService;

    @Autowired
    ArbitrageScanningService arbitrageScanningService;

    @Autowired
    @Qualifier("BaseTradeService")
    BaseTradeService baseTradeService;

    @GetMapping("/portfoliobalance/client")
    public RootResponse getClientPortfolioBalance(@RequestParam("userId") int userId) {
        return new RootResponse(HttpStatus.OK, "Balance retrieved for user id " + userId,
                clientPortfolioMapper.getCurrentValue(userId));
    }

    @GetMapping("/portfoliobalance/group")
    public RootResponse getGroupPortfolioBalance() {
        return new RootResponse(HttpStatus.OK, "Balanced retrieved for the group portfolio.",
                groupPortfolioMapper.getCurrentValue());
    }

    @GetMapping("/arbitrageopportunities")
    public RootResponse getCurrentArbitrageOpportunities(@RequestParam(value = "base", required = false) String base,
                                                         @RequestParam(value = "counter", required = false) String counter)
            throws Exception {

        List<Exchange> exchanges = exchangeService.generateExchangesList();

        if (base == null || counter == null)
            return new RootResponse(HttpStatus.OK, "Best arbitrage opportunities for all currency pairs.",
                    arbitrageScanningService.getBestArbitrageOpportunitiesForAllCurrencies(exchanges));
        else
            return new RootResponse(HttpStatus.OK, "Best arbitrage opportunity for pair " + base + "/" + counter + ".",
                arbitrageScanningService.getBestArbitrageOpportunitiesForPair(exchanges, new CurrencyPair(base, counter)));
    }



    @GetMapping("/trade/fee")
    public RootResponse getFees(@RequestParam("fromexchange") String fromExchangeName,
                                @RequestParam("toexchange") String toExchangeName,
                                @RequestParam("base") String base,
                                @RequestParam("counter") String counter,
                                @RequestParam(value = "amount", required = false, defaultValue = "0.0") String amount) {

        Exchange[] exchange = new Exchange[2];
        String packagePath = "org.knowm.xchange.";

        String fullyQualifiedBaseExchangeName = packagePath
                + fromExchangeName.toLowerCase()
                + "."
                + fromExchangeName
                + "Exchange";
        exchange[0] = ExchangeFactory.INSTANCE.createExchange(fullyQualifiedBaseExchangeName);

        String fullyQualifiedCounterExchangeName = packagePath
                + toExchangeName.toLowerCase()
                + "."
                + toExchangeName
                + "Exchange";
        exchange[1] = ExchangeFactory.INSTANCE.createExchange(fullyQualifiedCounterExchangeName);

        CurrencyPair cp = new CurrencyPair(base, counter);
        BigDecimal feeFromExchange = baseTradeService.calculateTradingFees(exchange[0], cp, new BigDecimal(amount));
        BigDecimal feeToExchange = baseTradeService.calculateTradingFees(exchange[1], cp, new BigDecimal(amount));
        BigDecimal totalFee = feeFromExchange.add(feeToExchange);
        String message = "Fee to trade " + base + "/" + counter + " on " +  fromExchangeName + " and buy "
                + base + " on " + toExchangeName + " is " + totalFee.toPlainString() + " " + base + " plus miners' fees.";
        return new RootResponse(message, totalFee);

        // TODO Subtract total fee from high bid, then subtract this difference from the low ask. If positive, disregard trade.
    }
}
