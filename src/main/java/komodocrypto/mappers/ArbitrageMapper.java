package komodocrypto.mappers;

import komodocrypto.model.arbitrage.ArbitrageModel;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;

@Mapper
public interface ArbitrageMapper {

    @Select("SELECT * FROM `komodo_crypto`.`arbitrage_opportunities`;")
    @Results(id = "ArbitrageModelResultMap", value = {
            @Result(property = "timestamp", column = "timestamp"),
            @Result(property = "currencyPair", column = "currency_pair"),
            @Result(property = "difference", column = "difference"),
            @Result(property = "lowAsk", column = "low_ask"),
            @Result(property = "highBid", column = "high_bid"),
            @Result(property = "lowAskExchange", column = "low_ask_exchange"),
            @Result(property = "highBidExchange", column = "high_bid_exchange")
    })
    public ArrayList<ArbitrageModel> getData();

    @Insert("INSERT INTO `komodo_crypto`.`arbitrage_opportunities` " +
            "(`timestamp`, `currency_pair`, `difference`, `low_ask`, `high_bid`, `low_ask_exchange`, `high_bid_exchange`) " +
            "VALUES (#{timestamp}, #{currencyPair}, #{difference}, #{lowAsk}, #{highBid}, #{lowAskExchange}, #{highBidExchange});")
    public int addArbitrageData(ArbitrageModel arbitrageOppData);
    
}
