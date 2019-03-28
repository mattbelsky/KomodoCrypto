package komodocrypto.mappers;

import komodocrypto.model.user.User;
import org.apache.ibatis.annotations.*;

import java.sql.SQLIntegrityConstraintViolationException;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO `komodo_crypto`.`users` (`username`, `password`, `email`) VALUES (#{username}, #{password}, #{email});")
    public int addUser(User user) throws SQLIntegrityConstraintViolationException;

    @Select("SELECT * FROM `komodo_crypto`.`users` WHERE `username` = #{username}")
    @Results(id = "userResult", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "accountNonExpired", column = "account_non_expired"),
            @Result(property = "credentialsNonExpired", column = "credentials_non_expired"),
            @Result(property = "accountNonLocked", column = "account_non_locked")
    })
    public User getUserByUsername(String username);

}
