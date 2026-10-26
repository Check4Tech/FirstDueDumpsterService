package check4tech.firstdueresponseservice.security;

import check4tech.firstdueresponseservice.Entity.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/** Who is calling, and which location they belong to. A location is the tenant boundary. */
public final class LocationAccess {

    private LocationAccess() {
    }

    public static Optional<AuthenticatedEmployee> current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedEmployee employee) {
            return Optional.of(employee);
        }
        return Optional.empty();
    }

    public static Long currentLocationId() {
        return current().map(AuthenticatedEmployee::locationId).orElse(null);
    }

    public static Long currentEmployeeId() {
        return current().map(AuthenticatedEmployee::id).orElse(null);
    }

    public static boolean isAdmin() {
        return current().map(e -> e.role() == Role.ADMIN).orElse(false);
    }

    /** Throws 403 unless the id is the caller's own location. */
    public static void requireOwnLocation(Long locationId) {
        Long own = currentLocationId();
        if (own == null || locationId == null || !own.equals(locationId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed.");
        }
    }
}
