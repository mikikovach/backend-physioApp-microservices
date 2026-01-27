package it.eng.api_gateway;


import it.eng.api_gateway.config.JwtUtil;
import it.eng.api_gateway.util.HeaderMapRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

//@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/signup"
    );
    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if(isPublic(path)) {
            System.out.println("JWT FILTER GATEWAY: Public path accessed: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        System.out.println("JWT FILTER GATEWAY: " + request.getMethod() + " " + request.getRequestURI());

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            System.out.println("JWT FILTER GATEWAY: Missing or invalid Authorization header");
            return;
        }
        try {

            final String jwt = authHeader.substring(7);
            final String userEmail = jwtUtil.extractUsername(jwt);
            System.out.println("JWT FILTER GATEWAY: Extracted user email: " + userEmail);


                HeaderMapRequestWrapper wrapper = new HeaderMapRequestWrapper(request);
                wrapper.addHeader("X-User-email", userEmail);

                filterChain.doFilter(wrapper, response);
        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occurred: " + e.getMessage());
//            handlerExceptionResolver.resolveException(request, response, null, e);
        }


    }
}
