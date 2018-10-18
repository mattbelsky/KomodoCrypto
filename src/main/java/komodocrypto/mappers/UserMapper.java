package komodocrypto.mappers;

import komodocrypto.model.user.User;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;

@Mapper
public interface UserMapper {

    String GET_USERS = "SELECT * FROM `komodo_crypto`.`users`;";

    String UPDATE_USER = "UPDATE `komodo_crypto`.`users` SET `first_name` = #{first_name}, `last_name` = #{last_name}, `password` = #{password}, " +
            "`email` = #{email} WHERE `user_id` = #{user_id};";

    String CREATE_USER = "INSERT INTO `komodo_crypto`.`users` (`user_id`, `first_name`, `last_name`, `password`, `email`, `userSettings_id`) " +
            "VALUES (#{user_id}, #{first_name}, #{last_name}, #{password}, #{email}, #{userSettings_id});";

    String GET_USER_BY_ID = "SELECT * FROM `komodo_crypto`.`users` WHERE `user_id` = #{user_id};";

    String GET_USER_BY_FIRSTNAME_LASTNAME = "SELECT * FROM `komodo_crypto`.`users` WHERE `first_name` = #{arg0} AND last_name = #{arg1} " +
            "ORDER BY created DESC limit 1;";


    @Select(GET_USERS)
    public ArrayList<User> getUsers();

    @Update(UPDATE_USER)
    public int updateUser(User user);

    @Insert(CREATE_USER)
    public int createUser(User user);

    @Select(GET_USER_BY_ID)
    public User getUserByID(int user_id);

    @Select(GET_USER_BY_FIRSTNAME_LASTNAME)
    public User getUserByFirstName_LastName(String first_name, String last_name);

}
