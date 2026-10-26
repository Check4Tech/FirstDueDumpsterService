package check4tech.firstdueresponseservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-client sliding-window throttle for unauthenticated and credential-guessing endpoints
 * (login, password reset, refresh, PIN checks, public forms). In-memory, per instance.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private record Rule(String bucket, int maxRequests, long windowMs) {
    }

    private static final Rule AUTH = new Rule("auth", 20, 60_000);

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();
    private volatile long lastSweep = System.currentTimeMillis();

    private static Rule ruleFor(HttpServletRequest request) {
        if ("POST".equals(request.getMethod()) && request.getRequestURI().matches("^/auth/location/[^/]+/login$")) {
            return AUTH;
        }
        return null;
    }

    /** The reverse proxy appends the real peer address last; earlier entries are client-controlled. */
    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] parts = forwarded.split(",");
            return parts[parts.length - 1].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Rule rule = "OPTIONS".equals(request.getMethod()) ? null : ruleFor(request);
        if (rule != null && !allow(rule, clientIp(request))) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(rule.windowMs() / 1000));
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"message\":\"Too many requests. Try again shortly.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean allow(Rule rule, String ip) {
        long now = System.currentTimeMillis();
        sweep(now);
        Deque<Long> window = hits.computeIfAbsent(rule.bucket() + "|" + ip, k -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() > rule.windowMs()) {
                window.pollFirst();
            }
            if (window.size() >= rule.maxRequests()) {
                return false;
            }
            window.addLast(now);
            return true;
        }
    }

    private void sweep(long now) {
        if (now - lastSweep < 300_000) {
            return;
        }
        lastSweep = now;
        hits.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                Long last = e.getValue().peekLast();
                return last == null || now - last > 120_000;
            }
        });
    }
}
