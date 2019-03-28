package komodocrypto.mappers;

import komodocrypto.model.database.CurrencyPairs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CurrencyPairsMapper {

    String GET_CURRENCY_PAIR_ID_BY_CURRENCY_ID = "SELECT `currency_pair_id` FROM `komodo_crypto`.`currency_pairs` " +
            "WHERE `currency_id_1` = #{arg0} AND `currency_id_2` = #{arg1};";
    String GET_ALL_DATA =   "SELECT * FROM `komodo_crypto`.`currency_pairs`;";

    @Select(GET_ALL_DATA)
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "currencyIdBase", column = "currency_id_1"),
            @Result(property = "currencyIdCounter", column = "currency_id_2"),
            @Result(property = "currencySymbolBase", column = "currency_symbol_1"),
            @Result(property = "currencySymbolCounter", column = "currency_symbol_2")
    })
    public List<CurrencyPairs> getAllCurrencyPairs();

    @Select(GET_CURRENCY_PAIR_ID_BY_CURRENCY_ID)
    public int getCurrencyPairId(int idFrom, int idTo);
    
}