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
@Order(3)
public class RequestSizeFilter extends OncePerRequestFilter {

    @Value("${tslib.request.max-size-bytes:1048576}")
    private long maxSizeBytes;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        int contentLength = request.getContentLength();
        if (contentLength > maxSizeBytes) {
            response.setStatus(413);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Payload Too Large\",\"message\":\"Request body exceeds the maximum allowed size.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
