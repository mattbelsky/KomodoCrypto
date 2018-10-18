package komodocrypto.mappers.database;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CurrencyMapper {

    @Select("SELECT `currency_id` from `komodo_crypto`.`currency` WHERE `symbol` = #{symbol};")
    public int getIdBySymbol(String symbol);
}
