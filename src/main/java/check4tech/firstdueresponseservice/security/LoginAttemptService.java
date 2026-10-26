package check4tech.firstdueresponseservice.security;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Temporary lockout of an account after repeated failed logins. */
@Component
public class LoginAttemptService {

    private static final int MAX_FAILURES = 8;
    private static final long LOCK_MS = 15 * 60_000L;

    private record State(int failures, long lockedUntil) {
    }

    private final Map<String, State> states = new ConcurrentHashMap<>();

    private static String key(String orgKey, String username) {
        return (orgKey == null ? "" : orgKey.trim().toLowerCase(Locale.ROOT)) + "|"
                + (username == null ? "" : username.trim().toLowerCase(Locale.ROOT));
    }

    public boolean isLocked(String orgKey, String username) {
        State s = states.get(key(orgKey, username));
        return s != null && s.lockedUntil() > System.currentTimeMillis();
    }

    public void recordFailure(String orgKey, String username) {
        long now = System.currentTimeMillis();
        states.merge(key(orgKey, username), new State(1, 0), (old, ignored) -> {
            if (old.lockedUntil() > 0 && old.lockedUntil() <= now) {
                return new State(1, 0);
            }
            int failures = old.failures() + 1;
            return new State(failures, failures >= MAX_FAILURES ? now + LOCK_MS : old.lockedUntil());
        });
        if (states.size() > 50_000) {
            states.entrySet().removeIf(e -> e.getValue().lockedUntil() != 0 && e.getValue().lockedUntil() < now);
        }
    }

    public void recordSuccess(String orgKey, String username) {
        states.remove(key(orgKey, username));
    }
}
