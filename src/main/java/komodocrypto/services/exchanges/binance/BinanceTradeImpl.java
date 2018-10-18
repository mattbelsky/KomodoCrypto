//package komodocrypto.services.exchanges.binance;
//
//import komodocrypto.configuration.exchange_utils.BinanceUtil;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import quickfix.fix44.OrderStatusRequest;
//
//import java.util.List;
//
//@Service
//public class BinanceTradeImpl {
//
//    private final Logger logger = LoggerFactory.getLogger(getClass());
//
//    @Autowired
//    BinanceUtil binanceUtil;
//
//    /**
//     * Prints out potentailly relevant trade info
//     */
//    public void getTradeInfo() {
//        BinanceApiRestClient client = binanceUtil.createExchange();
//
//        // Getting list of open orders
//        List<Order> openOrders = client.getOpenOrders(new OrderRequest("LINKETH"));
//        System.out.println(openOrders);
//
//        // Getting list of all orders with a limit of 10
//        List<Order> allOrders = client.getAllOrders(new AllOrdersRequest("LINKETH").limit(10));
//        System.out.println(allOrders);
//
//        // Get status of a particular order
//        Order order = client.getOrderStatus(new OrderStatusRequest("LINKETH", 751698L));
//        System.out.println(order);
//
////        // Placing a test LIMIT order
////        client.newOrderTest(limitBuy("LINKETH", TimeInForce.GTC, "1000", "0.0001"));
////
////        // Placing a real LIMIT order
////        NewOrderResponse newOrderResponse = client.newOrder(limitBuy("LINKETH", TimeInForce.GTC, "1000", "0.0001").newOrderRespType(NewOrderResponseType.FULL));
////        System.out.println(newOrderResponse);
//    }
//
//    /**
//     * Submit test market order to binance.
//     *
//     * @param pair String of asset pair to be traded e.g. BTCLTC, BTCETH, ETHBTC (order matters!)
//     * @param amount amount of asset to be traded
//     */
//    public void testMarketOrder(String pair, String amount) {
//        // Connect to ExchangeData
//        BinanceApiRestClient client = binanceUtil.createExchange();
//
//        // Place test order
//        logger.info("Placing TEST market order between " + pair + " for " + amount);
//        client.newOrderTest(marketBuy(pair, amount));
//
//        // TODO do we want to log test orders to DB?
//    }
//
//    /**
//     * Submit REAL market order to Binance.
//     *
//     * @param pair String of asset pair to be traded e.g. BTCLTC, BTCETH, ETHBTC (order matters!)
//     * @param amount amount of asset to be traded
//     */
//    public List<Trade> placeMarketOrder(String pair, String amount) {
//        // Connect to ExchangeData
//        BinanceApiRestClient client = binanceUtil.createExchange();
//
//        // Place real market order
//        logger.info("Placing REAL market order between " + pair + " for " + amount + " on Binance");
//        NewOrderResponse newOrderResponse = client.newOrder(
//                marketBuy(pair, amount).newOrderRespType(NewOrderResponseType.FULL));
//        // List of trade fills
//        List<Trade> fills = newOrderResponse.getFills();
//        logger.info("Order ID: " + newOrderResponse.getClientOrderId());
//
//        // TODO log trade to internal DB
//
//        return fills;
//    }
//
//    /**
//     * Cancel Binance order
//     *
//     * @param pair String of asset pair to be traded e.g. BTCLTC, BTCETH, ETHBTC (order matters)
//     * @param orderId Long orderId to be cancelled
//     * @throws BinanceApiException if cancel is unsuccessful
//     */
//    public void cancelOrder(String pair, Long orderId) throws BinanceApiException {
//        // Connect to ExchangeData
//        BinanceApiRestClient client = binanceUtil.createExchange();
//
//        logger.info("Attempting to cancel order " + orderId + "...");
//        // Canceling an order
//        try {
//            client.cancelOrder(new CancelOrderRequest(pair, orderId));
//            logger.info("Order " + orderId + " successfully cancelled.");
//        } catch (BinanceApiException e) {
//            throw new BinanceApiException("Unable to cancel " + pair + " order " + orderId);
//        }
//    }
//
//}