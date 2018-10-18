package komodocrypto.mappers.database;

import komodocrypto.model.arbitrage.ArbitrageTradeHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArbitrageTradeHistoryMapper {

    String GET_ALL_DATA = "SELECT * FROM `komodo_crypto`.`arbitrage_trade_history` ORDER BY `arbitrage_id` DESC;";
    String INSERT_NEW_DATA = "INSERT INTO `komodo_crypto`.`arbitrage_trade_history   ` " +
            "(`arbitrage_id`, `buy_transaction_id`, `sell_transaction_id`, `currency_pair_id`, `sell_price`, `sell_amount`, `buy_price`, `buy_amount`, `status`, `buy_exchange_id`, `sell_exchange_id`) " +
            "VALUES (#{arbitrage_id}, #{buy_transaction_id}, #{sell_transaction_id}, #{currency_pair_id}, #{sell_price}, #{sell_amount}, #{buy_price}, #{buy_amount}, #{status}, #{buy_exchange_id}, #{sell_exchange_id};";
    String SELECT_LATEST_DATA = "SELECT * FROM `komodo_crypto`.`arbitrage_trade_history` ORDER BY `arbitrage_id` DESC LIMIT 1;";

    @Select(GET_ALL_DATA)
    public List<ArbitrageTradeHistory> getAllData();

    @Insert(INSERT_NEW_DATA)
    public ArbitrageTradeHistory insertNewData(ArbitrageTradeHistory data);

    @Select(SELECT_LATEST_DATA)
    public ArbitrageTradeHistory getLastEntry();



}
