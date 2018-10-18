package komodocrypto.controllers.api;

import komodocrypto.mappers.database.ClientPortfolioMapper;
import komodocrypto.mappers.database.GroupPortfolioMapper;
import komodocrypto.model.RootResponse;
import komodocrypto.services.arbitrage.ArbitrageScanningService;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ArbitrageController {

    @Autowired
    ClientPortfolioMapper clientPortfolioMapper;

    @Autowired
    GroupPortfolioMapper groupPortfolioMapper;

    @Autowired
    ArbitrageScanningService arbitrageScanningService;

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
    public RootResponse getCurrentArbitrageOpportunities(@RequestParam("from") String from,
                                                         @RequestParam("to") String to) throws IOException {
        return new RootResponse(HttpStatus.OK, "Current arbitrage opportunities for pair " + from + "/" + to + ".",
                arbitrageScanningService.getExchangeNames(new CurrencyPair(from, to)));
    }
}
