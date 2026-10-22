package ai.foresignal.isolation;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Puts the thread serving a request in {@link WorkRole#API} for as long as it serves it, so the request's
 * database work draws only from the API pool. Runs first, ahead of CORS and authentication, which read the
 * database too; and on every dispatch, including the asynchronous ones that continue a streamed response.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiWorkRoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try (WorkRole.Scope ignored = WorkRole.enter(WorkRole.API)) {
            chain.doFilter(request, response);
        }
    }
}
