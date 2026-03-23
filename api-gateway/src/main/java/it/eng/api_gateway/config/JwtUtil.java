package it.eng.api_gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


@Component
@Slf4j
public class JwtUtil {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    public Claims validateToken(String token) {

        log.info("Validating token with secret key: " + secretKey);

        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public String extractUsername(String token) {
        return validateToken(token).getSubject();
    }

    public boolean isExpired(String token) {
        return validateToken(token).getExpiration().before(new Date());
    }

    public List<String> extractRoles(String token) {
      return validateToken(token).get("roles", List.class);
    }
}
