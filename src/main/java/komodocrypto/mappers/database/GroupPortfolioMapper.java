package komodocrypto.mappers.database;

import komodocrypto.model.database.GroupPortfolio;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface GroupPortfolioMapper {

    @Results(id = "GroupPortfolioResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "deposit_value", property = "depositValue"),
            @Result(column = "current_value", property = "currentValue"),
            @Result(column = "num_investors", property = "numInvestors"),
            @Result(column = "timestamp", property = "timestamp")
    })
    @Select("SELECT * FROM `komodo_crypto`.`group_portfolio`;")
    public List<GroupPortfolio> getAllEntries();

    @Insert("INSERT INTO `komodo_crypto`.`group_portfolio` " +
            "(`deposit_value`, `current_value`, `num_investors`) " +
            "VALUES (#{depositValue}, #{currentValue}, #{numInvestors})")
    public int addEntry(GroupPortfolio groupPortfolio);

    @Select("SELECT `num_investors` FROM `komodo_crypto`.`group_portfolio` " +
            "ORDER BY `group_portfolio_id` DESC LIMIT 1;")
    public int getNumInvestors();

    @Select("SELECT `current_value` FROM `komodo_crypto`.`group_portfolio` ORDER BY `timestamp` DESC LIMIT 1;")
    public BigDecimal getCurrentValue();
}
