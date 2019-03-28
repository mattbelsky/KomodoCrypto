package komodocrypto.services.users;

import komodocrypto.mappers.UserMapper;
import komodocrypto.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean usernameNotNull(User user) {
        return user.getUsername() != null;
    }

    public boolean passwordNotNull(User user) {
        return user.getPassword() != null;
    }

    public boolean emailNotNull(User user) {
        return user.getEmail() != null;
    }

    // Ensures that the email address matches the specified pattern.
    public boolean validateEmail(User user) {

        Pattern p = Pattern.compile(".+@.+[.].{2,}", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(user.getEmail());
        return m.matches();
    }

    public boolean usernameIsUnique(User user) {

        String username = user.getUsername();
        return userMapper.getUserByUsername(username) == null;
    }

    public User addUser(User user) throws SQLIntegrityConstraintViolationException {

        try {
            String passwordEncrypted = bCryptPasswordEncoder.encode(user.getPassword());
            user.setPassword(passwordEncrypted);
            userMapper.addUser(user);
        } catch (SQLIntegrityConstraintViolationException e) {
            // If the username is not unique, rethrows the exception for handling. This check is added for safety in
            // case usernameIsUnique() was never called.
            throw e;
        }
        return userMapper.getUserByUsername(user.getUsername());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userMapper.getUserByUsername(username);
    }
}
