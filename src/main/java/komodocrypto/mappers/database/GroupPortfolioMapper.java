package komodocrypto.mappers.database;

import komodocrypto.model.database.GroupPortfolio;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface GroupPortfolioMapper {

    String GET_ALL_ENTRIES = "SELECT * FROM `komodo_crypto`.`group_portfolio`;";

    String INSERT_ENTRY = "INSERT INTO `komodo_crypto`.`group_portfolio` " +
            "(`deposit_value`, `current_value`, `num_investors`) " +
            "VALUES (#{deposit_value}, #{current_value}, #{num_investors})";

    String GET_NUM_INVESTORS = "SELECT `num_investors` FROM `komodo_crypto`.`group_portfolio` " +
            "ORDER BY `group_portfolio_id` DESC LIMIT 1;";

    String GET_CURRENT_VALUE = "SELECT `current_value` FROM `komodo_crypto`.`group_portfolio` ORDER BY `timestamp` DESC LIMIT 1;";

    @Results(id = "GroupPortfolio", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "deposit_value", property = "depositValue"),
            @Result(column = "current_value", property = "currentValue"),
            @Result(column = "num_investors", property = "numInvestors"),
            @Result(column = "timestamp", property = "timestamp")
    })
    @Select(GET_ALL_ENTRIES)
    public List<GroupPortfolio> getAllEntries();

    @ResultMap("GroupPortfolio")
    @Insert(INSERT_ENTRY)
    public int addEntry(GroupPortfolio groupPortfolio);

    @Select(GET_NUM_INVESTORS)
    public int getNumInvestors();

    @Select(GET_CURRENT_VALUE)
    public BigDecimal getCurrentValue();
}
