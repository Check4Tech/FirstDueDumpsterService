package check4tech.firstdueresponseservice.security;

import check4tech.firstdueresponseservice.Entity.Employee;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

/**
 * Issues and verifies access tokens. The signing key comes from the {@code jwt.secret} property
 * (a Base64 string of at least 32 bytes); the service refuses to start without one.
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret:}") String secret,
                      @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret is not set. Provide a Base64 string of at least 32 bytes.");
        }
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(secret.trim());
        } catch (RuntimeException e) {
            throw new IllegalStateException("jwt.secret must be Base64 encoded.");
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("jwt.secret must decode to at least 32 bytes.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    public String generate(Employee employee) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(employee.getUsername())
                .claim("loc", employee.getLocation().getId())
                .claim("role", employee.getRole().name())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Empty when the token is malformed, expired, unsigned or signed with another key. */
    public Optional<Claims> verify(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
