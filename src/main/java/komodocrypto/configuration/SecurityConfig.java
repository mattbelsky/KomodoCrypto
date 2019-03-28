package komodocrypto.configuration;

import komodocrypto.security.JWTAuthenticationFilter;
import komodocrypto.security.JWTAuthorizationFilter;
import komodocrypto.security.RestAuthenticationEntryPoint;
import komodocrypto.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

import static komodocrypto.security.SecurityConstants.SIGN_UP_URL;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserService userService;

    @Autowired
    RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    AccessDeniedHandler accessDeniedHandler;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override // Autowiring configures the global parent Authentication Manager
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .authorizeRequests()

                // Allows anyone to access the sign-up URL.
                .antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()

                // Only allows authenticated users to access the API.
                .antMatchers("/api/**").authenticated()
                .and()

                // Custom JSON based authentication by POST of {"username":"<name>","password":"<password>"} which sets
                // the token header upon authentication.
                .addFilter(new JWTAuthenticationFilter(authenticationManager()))

                // Custom token based authentication based on the header previously given to the client
                .addFilterAfter(new JWTAuthorizationFilter(), JWTAuthenticationFilter.class)

                // Ensures that no HttpSession is created.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
