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

@Component
@Order(1)
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${tslib.api-key.enabled:false}")
    private boolean enabled;

    @Value("${tslib.api-key.value:}")
    private String apiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")) {
            chain.doFilter(request, response);
            return;
        }
        String provided = request.getHeader("X-API-Key");
        if (apiKey.isBlank() || !apiKey.equals(provided)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Unauthorized\",\"message\":\"Invalid or missing X-API-Key header\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
