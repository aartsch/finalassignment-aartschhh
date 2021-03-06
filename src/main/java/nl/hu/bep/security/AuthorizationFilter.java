package nl.hu.bep.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {

        boolean secure = containerRequestContext.getSecurityContext().isSecure();
        MySecurityContext msc = new MySecurityContext(null, secure);
        String header = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.split(" ")[1].trim();

            try {
                JwtParser parser = Jwts.parser().setSigningKey(AuthenticationResource.key);
                Claims claim = parser.parseClaimsJws(token).getBody();

                String userName = claim.getSubject();

                for (User user : User.getAllUsers()) {
                    if (user.getName().equals(userName)) {
                        msc = new MySecurityContext(user, secure);
                        break;
                    }
                }
            } catch (JwtException jwte) {
                System.out.println("JWT invalid for request!");
            }
        }
        containerRequestContext.setSecurityContext(msc);
    }
}
