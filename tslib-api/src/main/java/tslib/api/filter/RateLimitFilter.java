package tslib.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${tslib.rate-limit.enabled:false}")
    private boolean enabled;

    @Value("${tslib.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    // Each entry: [tokens, lastRefillEpochMs]
    private final ConcurrentHashMap<String, long[]> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }
        if (!tryConsume(clientIp(request))) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again in a minute.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean tryConsume(String ip) {
        long now = System.currentTimeMillis();
        long[] bucket = buckets.computeIfAbsent(ip, k -> new long[]{requestsPerMinute, now});
        synchronized (bucket) {
            long elapsed = now - bucket[1];
            long refill = (long) (elapsed * requestsPerMinute / 60_000.0);
            if (refill > 0) {
                bucket[0] = Math.min(requestsPerMinute, bucket[0] + refill);
                bucket[1] = now;
            }
            if (bucket[0] <= 0) return false;
            bucket[0]--;
            return true;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
