package komodocrypto.mappers.exchanges;

import komodocrypto.model.exchanges.ExchangeData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface ExchangeMapper {

    @Select("SELECT * FROM `komodo_crypto`.`exchanges`;")
    @Results(id = "ExchangeResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "exchangeName", column = "exchange_name"),
            @Result(property = "transferFee", column = "transfer_fee"),
            @Result(property = "buyFee", column = "buy_fee"),
            @Result(property = "sellFee", column = "sell_fee")
    })
    public List<ExchangeData> getExchanges();

    @Select("SELECT `exchange_name` FROM `komodo_crypto`.`exchanges`;")
    public ArrayList<String> getExchangeNames();

    @Select("SELECT `id` FROM `komodo_crypto`.`exchanges` WHERE `exchange_name` = #{exchangeName};")
    public int getExchangeIdByName(String exchangeName);
}
