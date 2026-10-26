package check4tech.firstdueresponseservice.security;

import check4tech.firstdueresponseservice.Entity.Employee;
import check4tech.firstdueresponseservice.Entity.Location;
import check4tech.firstdueresponseservice.Entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static String secret(char c) {
        return Base64.getEncoder().encodeToString(String.valueOf(c).repeat(40).getBytes());
    }

    private static Employee employee() {
        Location location = new Location();
        location.setId(3L);
        Employee e = new Employee();
        e.setId(9L);
        e.setUsername("JDOE");
        e.setRole(Role.ADMIN);
        e.setLocation(location);
        return e;
    }

    @Test
    void tokenRoundTripKeepsLocationAndRole() {
        JwtService jwt = new JwtService(secret('a'), 60_000);
        Optional<Claims> claims = jwt.verify(jwt.generate(employee()));
        assertTrue(claims.isPresent());
        assertEquals("JDOE", claims.get().getSubject());
        assertEquals(3L, claims.get().get("loc", Long.class));
        assertEquals("ADMIN", claims.get().get("role", String.class));
    }

    @Test
    void rejectsTamperedExpiredForeignAndUnsignedTokens() {
        JwtService jwt = new JwtService(secret('a'), 60_000);
        String token = jwt.generate(employee());
        assertFalse(jwt.verify(token.substring(0, token.length() - 3) + "abc").isPresent());
        assertFalse(jwt.verify(new JwtService(secret('a'), -1_000).generate(employee())).isPresent());
        assertFalse(jwt.verify(new JwtService(secret('b'), 60_000).generate(employee())).isPresent());
        String unsigned = Jwts.builder().subject("JDOE").claim("loc", 3L).compact();
        assertFalse(jwt.verify(unsigned).isPresent());
        assertFalse(jwt.verify("not.a.token").isPresent());
        assertFalse(jwt.verify("").isPresent());
    }

    @Test
    void refusesToStartWithoutAStrongSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtService("", 1000));
        assertThrows(IllegalStateException.class, () -> new JwtService("   ", 1000));
        assertThrows(IllegalStateException.class, () -> new JwtService(Base64.getEncoder().encodeToString("short".getBytes()), 1000));
        assertThrows(IllegalStateException.class, () -> new JwtService("***not base64***", 1000));
    }
}
