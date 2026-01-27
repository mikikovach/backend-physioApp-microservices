package it.eng.api_gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.eng.api_gateway.config.ErrorCodeMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@AllArgsConstructor
@Order(-2) // Higher precedence than default exception handlers
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final ErrorCodeMapper errorCodeMapper;

//    @Override
//    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
//
//
//        if (!(ex instanceof WebClientResponseException webEx)) {
//            return Mono.error(ex); // pustamo dalje na default error handling webflux-a
//        }
//
//        ServerHttpResponse response = exchange.getResponse();
//        ServerHttpRequest request = exchange.getRequest();
//
//        HttpStatus status = HttpStatus.valueOf(webEx.getStatusCode().value());
//        response.setStatusCode(status);
//        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//
//        // Pokušaj da pročitaš thin error iz body-ja
//        ThinErrorResponse thinError = null;
//        try {
//            if (webEx.getResponseBodyAsByteArray().length > 0) {
//                thinError = objectMapper.readValue(
//                        webEx.getResponseBodyAsByteArray(),
//                        ThinErrorResponse.class
//                );
//            }
//        } catch (Exception e) {
//            log.debug("Failed to parse thin error from downstream", e);
//        }
//
//        // Prevod u ApiError
//        String message;
//        if (thinError != null && thinError.errorCode() != null) {
//            message = errorCodeMapper.resolveMessage(thinError.errorCode());
//        } else {
//            message = "Unexpected error occurred";
//        }
//
//        ApiError apiError = new ApiError(
//                status,
//                message,
//                request.getPath().value()
//        );
//
//        // Serijalizacija i slanje odgovora
//        byte[] bytes;
//        try {
//            bytes = objectMapper.writeValueAsBytes(apiError);
//        } catch (JsonProcessingException e) {
//            return Mono.error(e);
//        }
//
//        DataBuffer buffer = response.bufferFactory().wrap(bytes);
//        return response.writeWith(Mono.just(buffer));
//    }


    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

}
