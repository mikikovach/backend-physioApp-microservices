package it.eng.auth_service.configs;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;


@Component
@Slf4j
public class UserContextFilter implements Filter {
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/signup"
    );
    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        log.info("USER CONTEXT FILTER: " + request.getMethod() + " " + request.getRequestURI());

        String path = request.getRequestURI();
        if (isPublic(path)) {
            log.info("USER CONTEXT FILTER: Public path accessed, skipping header validation");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }


        String userEmail = request.getHeader("X-User-Email");
        String userId = request.getHeader("X-User-Id") ;

        if (userEmail == null) {
            log.info("USER CONTEXT FILTER: Missing X-User-Email header");
            ((HttpServletResponse)servletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
            request.setAttribute("userEmail", userEmail);
            request.setAttribute("userId", Long.valueOf(userId));
            filterChain.doFilter(servletRequest, servletResponse);


    }
}
