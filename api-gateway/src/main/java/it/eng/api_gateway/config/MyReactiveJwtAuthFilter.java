package it.eng.api_gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import it.eng.api_gateway.error.ApiError;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@AllArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class MyReactiveJwtAuthFilter implements GlobalFilter{

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/signup",
            "/physios",
            "/slots"
    );
    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublic(path)) {
            log.info(" JWT FILTER REACTIVE GATEWAY: Public path accessed: " + path);
            return chain.filter(exchange);
        }

        HttpHeaders headers = exchange.getRequest().getHeaders();
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            String jwt = authHeader.substring(7);
            String userEmail = jwtUtil.extractUsername(jwt);
            Long userId = jwtUtil.validateToken(jwt).get("userId", Long.class);
            List<String> roles = jwtUtil.extractRoles(jwt);

            log.info(" JWT FILTER REACTIVE GATEWAY: Extracted user email: " + userEmail);

            if (path.startsWith("/admin") && (roles == null || !roles.contains("ROLE_ADMIN"))) {
                return returnResponseMessage(exchange, "Access denied: Admin role required", HttpStatus.FORBIDDEN);
            }

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-User-email", userEmail)
                            .header("X-User-Id", String.valueOf(userId))
                            .build())
                    .build();

            log.info(" JWT FILTER REACTIVE GATEWAY mutaded rq: " +  mutatedExchange.getRequest().getHeaders());

            return chain.filter(mutatedExchange);
        } catch (ExpiredJwtException e){
            return returnResponseMessage(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
        } catch (JwtException e) {
            return returnResponseMessage(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }


    }


    private Mono<Void> returnResponseMessage(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiError apiError = new ApiError(
                status,
                message,
                exchange.getRequest().getPath().value()
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(apiError);
        } catch (JsonProcessingException e) {
            return response.setComplete(); // fallback
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }



//    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
//        ServerHttpResponse response = exchange.getResponse();
//        response.setStatusCode(HttpStatus.UNAUTHORIZED);
//        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//
//        ApiError apiError = new ApiError(
//                HttpStatus.UNAUTHORIZED,
//                message,
//                exchange.getRequest().getPath().value()
//        );
//
//        byte[] bytes;
//        try {
//            bytes = objectMapper.writeValueAsBytes(apiError);
//        } catch (JsonProcessingException e) {
//            return response.setComplete(); // fallback
//        }
//
//        DataBuffer buffer = response.bufferFactory().wrap(bytes);
//        return response.writeWith(Mono.just(buffer));
//    }
}
