package komodocrypto.mappers;

import komodocrypto.model.cryptocompare.historical_data.Data;
import komodocrypto.model.cryptocompare.social_stats.Facebook;
import komodocrypto.model.cryptocompare.social_stats.Reddit;
import komodocrypto.model.cryptocompare.social_stats.Twitter;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Value;

@Mapper
public interface CryptoMapper {

    String dbName = "komodo_crypto";

    String fieldsValuesPeriodData =
            "(`time`, `fromCurrency`, `toCurrency`, `exchange`, `open`, `low`, `high`, `close`, `average`, `volumeFrom`, `volumeTo`) " +
            "VALUES (#{time}, #{fromCurrency}, #{toCurrency}, #{exchange}, #{open}, #{low}, #{high}, #{close}, #{average}, #{volumeFrom}, #{volumeTo});";

    String INSERT_PRICE_DAILY = "INSERT IGNORE INTO `komodo_crypto`.`daily` " + fieldsValuesPeriodData;
    String INSERT_PRICE_HOURLY = "INSERT IGNORE INTO `komodo_crypto`.`hourly` " + fieldsValuesPeriodData;
    String INSERT_PRICE_MINUTELY = "INSERT IGNORE INTO `komodo_crypto`.`minutely` " + fieldsValuesPeriodData;

    String SELECT_PRICE_DAILY = "SELECT * FROM komodo_crypto.daily;";
    String SELECT_PRICE_HOURLY = "SELECT * FROM komodo_crypto.hourly;";
    String SELECT_PRICE_MINUTELY = "SELECT * FROM komodo_crypto.minutely;";

    String SELECT_PRICE_DAILY_CONDITIONAL = "SELECT * FROM `komodo_crypto`.`daily` " +
            "WHERE fromCurrency = #{param1} AND toCurrency = #{param2} AND exchange = #{param3};";
    String SELECT_PRICE_HOURLY_CONDITIONAL = "SELECT * FROM `komodo_crypto`.`hourly` " +
            "WHERE fromCurrency = #{param1} AND toCurrency = #{param2} AND exchange = #{param3};";
    String SELECT_PRICE_MINUTELY_CONDITIONAL = "SELECT * FROM `komodo_crypto`.`minutely` " +
            "WHERE fromCurrency = #{param1} AND toCurrency = #{param2} AND exchange = #{param3};";

    String SELECT_DATA_BY_CURRENCY = "SELECT * FROM komodo_crypto.daily WHERE fromCurrency = #{currency} " +
            "UNION SELECT * FROM komodo_crypto.hourly WHERE fromCurrency = #{currency} " +
            "UNION SELECT * FROM komodo_crypto.minutely WHERE fromCurrency = #{currency};";
    String SELECT_DATA_BY_EXCHANGE = "SELECT * FROM komodo_crypto.daily WHERE exchange = #{exchange} " +
            "UNION SELECT * FROM komodo_crypto.hourly WHERE exchange = #{exchange} " +
            "UNION SELECT * FROM komodo_crypto.minutely WHERE exchange = #{exchange};";
    String SELECT_DATA_DAILY_BY_CURRENCY = "SELECT * FROM komodo_crypto.daily WHERE fromCurrency = #{currency};";
    String SELECT_DATA_HOURLY_BY_CURRENCY = "SELECT * FROM komodo_crypto.hourly WHERE fromCurrency = #{currency};";
    String SELECT_DATA_MINUTELY_BY_CURRENCY = "SELECT * FROM komodo_crypto.minutely WHERE fromCurrency = #{currency};";
    String SELECT_DATA_DAILY_BY_EXCHANGE = "SELECT * FROM komodo_crypto.daily WHERE exchange = #{exchange};";
    String SELECT_DATA_HOURLY_BY_EXCHANGE = "SELECT * FROM komodo_crypto.hourly WHERE exchange = #{exchange};";
    String SELECT_DATA_MINUTELY_BY_EXCHANGE = "SELECT * FROM komodo_crypto.minutely WHERE exchange = #{exchange};";
    String SELECT_DATA_BY_CURRENCY_AND_EXCHANGE = "SELECT * FROM komodo_crypto.daily WHERE fromCurrency = #{arg0} AND exchange = #{arg1} " +
            "UNION SELECT * FROM komodo_crypto.hourly WHERE fromCurrency = #{arg0} AND exchange = #{arg1}" +
            "UNION SELECT * FROM komodo_crypto.minutely WHERE fromCurrency = #{arg0} AND exchange = #{arg1};";


    String SELECT_DATA_DAILY_BY_PAIR_SORTED = "SELECT * FROM komodo_crypto.daily WHERE fromCurrency = #{arg0} AND toCurrency= #{arg1} " +
                                              "GROUP BY time ORDER BY time ASC ;";

    String SELECT_MISSING_DAILY_BINANCE = "SELECT * FROM `komodo_crypto`.`daily` WHERE `open` = 0.0 AND `close` = 0.0 AND " +
            "`high` = 0.0 AND `low` = 0.0 AND `exchange` = 'Binance' ORDER BY `time` DESC;";
    String SELECT_MISSING_HOURLY_BINANCE = "SELECT * FROM `komodo_crypto`.`hourly` WHERE `open` = 0.0 AND `close` = 0.0 AND " +
            "`high` = 0.0 AND `low` = 0.0 AND `exchange` = 'Binance' ORDER BY `time` DESC;";
    String SELECT_MISSING_MINUTELY_BINANCE = "SELECT * FROM `komodo_crypto`.`minutely` WHERE `open` = 0.0 AND `close` = 0.0 AND " +
            "`high` = 0.0 AND `low` = 0.0 AND `exchange` = 'Binance' ORDER BY `time` DESC;";

    String UPDATE_DATA_DAILY = "UPDATE `komodo_crypto`.`daily` SET `open` = #{open}, `low` = #{low}, `high` = #{high}, " +
            "`close` = #{close}, `average` = #{average} WHERE `time` = #{arg0};";
    String UPDATE_DATA_HOURLY = "UPDATE `komodo_crypto`.`hourly` SET `open` = #{open}, `low` = #{low}, `high` = #{high}, " +
            "`close` = #{close}, `average` = #{average} WHERE `time` = #{arg0};";
    String UPDATE_DATA_MINUTELY = "UPDATE `komodo_crypto`.`minutely` SET `open` = #{open}, `low` = #{low}, `high` = #{high}, " +
            "`close` = #{close}, `average` = #{average} WHERE `time` = #{arg0};";

    String SELECT_TIME_DAILY = "SELECT time FROM komodo_crypto.daily " +
            "WHERE fromCurrency = #{param1} AND toCurrency = #{param2} AND exchange = #{param3} " +
            "ORDER BY time ASC;";
    String SELECT_TIME_HOURLY = "SELECT time FROM komodo_crypto.hourly " +
            "WHERE fromCurrency = #{param1} AND toCurrency = #{param2} AND exchange = #{param3} " +
            "ORDER BY time ASC;";
    String SELECT_TIME_MINUTELY = "SELECT time FROM komodo_crypto.minutely " +
            "WHERE fromCurrency = #{param1} AND toCurrency = #{param2} AND exchange = #{param3} " +
            "ORDER BY time ASC;";

    String COUNT_DAILY_RECORDS = "SELECT COUNT(id) FROM `komodo_crypto`.`daily` " +
            "WHERE `fromCurrency` = 'ETH' AND `toCurrency` = 'BTC' AND `exchange` = 'Bitstamp';";
    String COUNT_HOURLY_RECORDS = "SELECT COUNT(id) FROM `komodo_crypto`.`hourly` " +
            "WHERE `fromCurrency` = 'ETH' AND `toCurrency` = 'BTC' AND `exchange` = 'Bitstamp';";
    String COUNT_MINUTELY_RECORDS = "SELECT COUNT(id) FROM `komodo_crypto`.`minutely` " +
            "WHERE `fromCurrency` = 'ETH' AND `toCurrency` = 'BTC' AND `exchange` = 'Bitstamp';";

    String GET_LAST_TIMESTAMP_DAILY = "SELECT `time` FROM `komodo_crypto`.`daily` " +
            "WHERE `fromCurrency` = 'ETH' AND `toCurrency` = 'BTC' AND `exchange` = 'Binance' " +
            "ORDER BY `time` ASC LIMIT 1;";
    String GET_LAST_TIMESTAMP_HOURLY = "SELECT `time` FROM `komodo_crypto`.`hourly` " +
            "WHERE `fromCurrency` = 'ETH' AND `toCurrency` = 'BTC' AND `exchange` = 'Binance' " +
            "ORDER BY `time` ASC LIMIT 1;";
    String GET_LAST_TIMESTAMP_MINUTELY = "SELECT `time` FROM `komodo_crypto`.`minutely` " +
            "WHERE `fromCurrency` = 'ETH' AND `toCurrency` = 'BTC' AND `exchange` = 'Binance' " +
            "ORDER BY `time` ASC LIMIT 1;";

    String INSERT_PRICE_AGGREGATED_WEEKLY = "INSERT IGNORE INTO komodo_crypto.weekly " +
            "(`time`, `fromCurrency`, `toCurrency`, `exchange`, `open`, `low`, `high`, `close`, `average`, `volumeFrom`, `volumeTo`) " +
            "VALUES (" +
                "#{arg1}, " +
                "#{arg2}, " +
                "#{arg3}, " +
                "#{arg4}, " +
                "(SELECT open FROM komodo_crypto.hourly WHERE time = #{arg0}), " +
                "(SELECT MIN(low) FROM komodo_crypto.hourly WHERE (time >= #{arg0} AND time <= #{arg1})), " +
                "(SELECT MAX(high) FROM komodo_crypto.hourly WHERE (time >= #{arg0} AND time <= #{arg1})), " +
                "(SELECT close FROM komodo_crypto.hourly WHERE time = #{arg1}), " +
                "(SELECT AVG(average) FROM komodo_crypto.hourly WHERE (time >= #{arg0} AND time <= #{arg1})), " +
                "(SELECT volumeFrom FROM komodo_crypto.hourly WHERE time = #{arg0}), " +
                "(SELECT volumeTo FROM komodo_crypto.hourly WHERE time = #{arg1}) " +
            ");";

    String INSERT_TWITTER_DATA = "INSERT INTO `komodo_crypto`.`twitter` " +
            "(`time`, `currency`, `statuses`, `followers`, `favorites`, `lists`, `following`, `points`) " +
            "VALUES (#{time}, #{currency}, #{statuses}, #{followers}, #{favorites}, #{lists}, #{following}, #{points});";
    String INSERT_REDDIT_DATA = "INSERT INTO `komodo_crypto`.`reddit` " +
            "(`time`, `currency`, `subscribers`, `commentsPerDay`, `commentsPerHour`, `activeUsers`, `postsPerDay`, `postsPerHour`, `points`) " +
            "VALUES (#{time}, #{currency}, #{subscribers}, #{commentsPerDay}, #{commentsPerHour}, #{activeUsers}, #{postsPerDay}, #{postsPerHour}, #{points});";
    String INSERT_FACEBOOK_DATA = "INSERT INTO `komodo_crypto`.`facebook` " +
            "(`time`, `currency`, `talkingAbout`, `likes`, `points`) " +
            "VALUES (#{time}, #{currency}, #{talkingAbout}, #{likes}, #{points});";

    String SELECT_TWITTER_DATA = "SELECT * FROM komodo_crypto.twitter;";
    String SELECT_REDDIT_DATA = "SELECT * FROM komodo_crypto.reddit;";
    String SELECT_FACEBOOK_DATA = "SELECT * FROM komodo_crypto.facebook;";

    String INSERT_NEWS_DATA = "INSERT IGNORE INTO `komodo_crypto`.`news` " +
            "(`articleId`, `publishedOn`, `title`, `url`, `body`, `tags`, `categories`) " +
            "VALUES (#{articleId}, #{publishedOn}, #{title}, #{url}, #{body}, #{tags}, #{categories});";
    String SELECT_ALL_NEWS_DATA = "SELECT * FROM `komodo_crypto`.`news`;";
    String SELECT_NEWS_BY_CATEGORY = "SELECT * FROM `komodo_crypto`.`news` WHERE categories LIKE '%${category}%';";


    // Gets a list of currencies traded.
    @Select("SELECT `symbol` FROM `" + dbName + "`.`currencies`;")
    public String[] getCurrencies();

    // Gets a list of exchanges traded on.
    @Select("SELECT `exchange_name` FROM `" + dbName + "`.exchanges;")
    public String[] getExchanges();

    // Adds historical data by time period.
    @Insert(INSERT_PRICE_DAILY)
    public int addPriceDaily(Data data);

    @Insert(INSERT_PRICE_HOURLY)
    public int addPriceHourly(Data data);

    @Insert(INSERT_PRICE_MINUTELY)
    public int addPriceMinutely(Data data);


    // Gets hourly data between two specified timestamps
    @Insert(INSERT_PRICE_AGGREGATED_WEEKLY)
    public int aggregateWeekly(int startTime, int endTime, String fromCurrency, String toCurrency, String exchange);


    // Adds social media stats.
    @Insert(INSERT_TWITTER_DATA)
    public int addTwitter(Twitter twitterData);

    @Insert(INSERT_REDDIT_DATA)
    public int addReddit(Reddit redditData);

    @Insert(INSERT_FACEBOOK_DATA)
    public int addFacebook(Facebook facebookData);


    // Gets social media stats.
    @Select(SELECT_TWITTER_DATA)
    public Twitter[] getTwitter();

    @Select(SELECT_REDDIT_DATA)
    public Reddit[] getReddit();

    @Select(SELECT_FACEBOOK_DATA)
    public Facebook[] getFacebook();


    // Gets all historical data by time period.
    @Select(SELECT_PRICE_DAILY)
    public Data[] getPriceDaily();

    @Select(SELECT_PRICE_HOURLY)
    public Data[] getPriceHourly();

    @Select(SELECT_PRICE_MINUTELY)
    public Data[] getPriceMinutely();


    // Gets historical data by time period, currency pair, and exchange.
    @Select(SELECT_PRICE_DAILY_CONDITIONAL)
    public Data[] getPriceDailyConditional(String fromCurrency, String toCurrency, String exchange);

    @Select(SELECT_PRICE_HOURLY_CONDITIONAL)
    public Data[] getPriceHourlyConditional(String fromCurrency, String toCurrency, String exchange);

    @Select(SELECT_PRICE_MINUTELY_CONDITIONAL)
    public Data[] getPriceMinutelyConditional(String fromCurrency, String toCurrency, String exchange);


    // Gets timestamp by period
    @Select(SELECT_TIME_DAILY)
    public Integer[] getTimeDaily(String fromCurrency, String toCurrency, String exchange);

    @Select(SELECT_TIME_HOURLY)
    public Integer[] getTimeHourly(String fromCurrency, String toCurrency, String exchange);

    @Select(SELECT_TIME_MINUTELY)
    public Integer[] getTimeMinutely(String fromCurrency, String toCurrency, String exchange);


    // Counts the number of records by period
    @Select(COUNT_DAILY_RECORDS)
    public int countRecordsDaily(String fromCurrency, String toCurrency, String exchange);

    @Select(COUNT_HOURLY_RECORDS)
    public int countRecordsHourly(String fromCurrency, String toCurrency, String exchange);

    @Select(COUNT_MINUTELY_RECORDS)
    public int countRecordsMinutely(String fromCurrency, String toCurrency, String exchange);


    // Gets the last timestamp in a time period table.
    @Select(GET_LAST_TIMESTAMP_DAILY)
    public int getLastTimestampDaily(String fromCurrency, String toCurrency, String exchange);

    @Select(GET_LAST_TIMESTAMP_HOURLY)
    public int getLastTimestampHourly(String fromCurrency, String toCurrency, String exchange);

    @Select(GET_LAST_TIMESTAMP_MINUTELY)
    public int getLastTimestampMinutely(String fromCurrency, String toCurrency, String exchange);


    // Adds and retrieves unique news data.
    @Insert(INSERT_NEWS_DATA)
    public int addNews(komodocrypto.model.cryptocompare.news.Data newsData);

    @Select(SELECT_ALL_NEWS_DATA)
    public komodocrypto.model.cryptocompare.news.Data[] getNews();

    @Select(SELECT_NEWS_BY_CATEGORY)
    public komodocrypto.model.cryptocompare.news.Data[] getNewsByCategory(@Param("category") String category);


    // Gets historical price data by various criteria
    @Select(SELECT_DATA_BY_CURRENCY)
    public Data[] getDataByCurrency(String currency);

    @Select(SELECT_DATA_BY_EXCHANGE)
    public Data[] getDataByExchange(String exchange);

    @Select(SELECT_DATA_DAILY_BY_CURRENCY)
    public Data[] getDataDailyByCurrency(String currency);

    @Select(SELECT_DATA_HOURLY_BY_CURRENCY)
    public Data[] getDataHourlyByCurrency(String currency);

    @Select(SELECT_DATA_MINUTELY_BY_CURRENCY)
    public Data[] getDataMinutelyByCurrency(String currency);

    @Select(SELECT_DATA_DAILY_BY_EXCHANGE)
    public Data[] getDataDailyByExchange(String exchange);

    @Select(SELECT_DATA_HOURLY_BY_EXCHANGE)
    public Data[] getDataHourlyByExchange(String exchange);

    @Select(SELECT_DATA_MINUTELY_BY_EXCHANGE)
    public Data[] getDataMinutelyByExchange(String exchange);

    @Select(SELECT_DATA_BY_CURRENCY_AND_EXCHANGE)
    public Data[] getDataByCurrencyAndExchange(String currency, String exchange);

    @Select(SELECT_DATA_DAILY_BY_PAIR_SORTED)
    public Data[] getDataDailyByPairSorted(String fromCurrency, String toCurrency);


    // Gets rows that are missing CryptoCompare data.
    @Select(SELECT_MISSING_DAILY_BINANCE)
    public Data[] getMissingDailyBinance(String fromCurrency, String toCurrency);

    @Select(SELECT_MISSING_HOURLY_BINANCE)
    public Data[] getMissingHourlyBinance(String fromCurrency, String toCurrency);

    @Select(SELECT_MISSING_MINUTELY_BINANCE)
    public Data[] getMissingMinutelyBinance(String fromCurrency, String toCurrency);


    // Updates the database by time.
    @Update(UPDATE_DATA_DAILY)
    public int updateDataDaily(int time, Data entry);

    @Update(UPDATE_DATA_HOURLY)
    public int updateDataHourly(int time, Data entry);

    @Update(UPDATE_DATA_MINUTELY)
    public int updateDataMinutely(int time, Data entry);
}
