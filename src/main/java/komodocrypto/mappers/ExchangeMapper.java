package komodocrypto.mappers;

import komodocrypto.model.exchanges.ExchangeInOutLimits;
import komodocrypto.model.exchanges.ExchangeModel;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface ExchangeMapper {

    @Select("SELECT * FROM `komodo_crypto`.`exchanges`;")
    @Results(id = "ExchangeResultMap", value = {
            @Result(property = "exchangeName", column = "exchange_name"),
            @Result(property = "takerTradeFee", column = "taker_trade_fee"),
            @Result(property = "makerTradeFee", column = "maker_trade_fee")
    })
    public List<ExchangeModel> getExchanges();

    @Select("SELECT `exchange_name` FROM `komodo_crypto`.`exchanges`;")
    public ArrayList<String> getExchangeNames();

    @Select("SELECT `id` FROM `komodo_crypto`.`exchanges` WHERE `exchange_name` = #{exchangeName};")
    public int getExchangeIdByName(String exchangeName);

    @Select("SELECT * FROM `komodo_crypto`.`exchanges` WHERE `exchange_name` = #{exchangeName};")
    @ResultMap("ExchangeResultMap")
    public ExchangeModel getExchangeModelByName(String exchangeName);

    @Select("SELECT * FROM `komodo_crypto`.`exchange_inout_limits` limits " +
            "WHERE limits.`exchange_id` = " +
            "(SELECT `id` FROM `komodo_crypto`.`exchanges` WHERE `exchange_name` = #{arg0} LIMIT 1) " +
            "AND limits.`currency` = #{arg1};")
    @Results(id = "ExchangeInOutLimitsResultMap", value = {
            @Result(property = "depositMin", column = "deposit_min"),
            @Result(property = "withdrawalMin", column = "withdrawal_min"),
            @Result(property = "withdrawalMax", column = "withdrawal_max"),
            @Result(property = "withdrawalMaxPeriod", column = "withdrawal_max_period"),
            @Result(property = "withdrawalCurrencyEquivalent", column = "withdrawal_currency_equivalent")
    })
    public ExchangeInOutLimits getLimitsbyExchangeAndCurrency(String exchangeName, String currency);
}
