//package komodocrypto;
//
//import komodocrypto.mappers.CryptoMapper;
//import komodocrypto.services.data_collection.CacheableDataCollectionTasks;
//import komodocrypto.services.data_collection.CryptoCompareHistoricalService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class AppRunner implements CommandLineRunner {
//
//    Logger logger = LoggerFactory.getLogger(this.getClass());
//    CacheableDataCollectionTasks cacheableDataCollectionTasks;
//
//    public AppRunner(CacheableDataCollectionTasks cacheableDataCollectionTasks) {
//        this.cacheableDataCollectionTasks = cacheableDataCollectionTasks;
//    }
//
//    @Override
//    public void run(String[] args) {
//
//        String[][] tradingPairs = cacheableDataCollectionTasks.getTradingPairs();
//        logger.info("Trading pairs retrieved and cached:\n" + tradingPairs.toString());
//
//        String[] exchanges = cacheableDataCollectionTasks.getExchanges();
//        logger.info("ExchangeData list retrieved and cached:\n" + exchanges.toString());
//    }
//}
