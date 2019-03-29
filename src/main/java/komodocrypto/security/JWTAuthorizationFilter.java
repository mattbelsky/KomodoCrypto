package komodocrypto.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;

import static komodocrypto.security.SecurityConstants.HEADER_STRING;
import static komodocrypto.security.SecurityConstants.SECRET;
import static komodocrypto.security.SecurityConstants.TOKEN_PREFIX;

// This filter receives a JWT in the HTTP request header, verifies it, and passes a valid Authentication to the
// SecurityContext.
public class JWTAuthorizationFilter extends GenericFilterBean {

    // This method is necessary when extending GenericFilterBean.
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // Gets the "Authorization" header from the HTTP request.
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String header = httpServletRequest.getHeader(HEADER_STRING);

        // If the header is null or isn't a bearer token, passes the request and response to the next filter in the chain.
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // Passes the Authentication object to the SecurityContext and passes the request and response to the next filter.
        UsernamePasswordAuthenticationToken authentication = getAuthentication(httpServletRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    // Creates a new Authentication object containing the username by decoding the JWT from the "Authorization" header
    // in the request.
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {

        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            // Parse the token.
            String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                    .build()
                    .verify(token.replace(TOKEN_PREFIX, ""))
                    .getSubject();

            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }
            return null;
        }
        return null;
    }
}
