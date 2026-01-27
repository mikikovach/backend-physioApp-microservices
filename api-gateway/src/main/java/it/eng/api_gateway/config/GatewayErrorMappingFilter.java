package it.eng.api_gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.eng.api_gateway.error.ApiError;
import it.eng.api_gateway.error.ThinErrorResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Order(-10)
@AllArgsConstructor
@Slf4j
public class GatewayErrorMappingFilter implements GlobalFilter {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        log.info("GatewayErrorMappingFilter invoked");

        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse =
                new ServerHttpResponseDecorator(originalResponse) {

                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                        return super.writeWith(
                                Flux.from(body).flatMap(dataBuffer -> {

                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    DataBufferUtils.release(dataBuffer);

                                    try {
                                        // parsiramo ErrorResponse iz Slots servisa
                                        ThinErrorResponse errorResponse =
                                                objectMapper.readValue(content, ThinErrorResponse.class);

                                        // OVDE uzimamo status – JEDINO ispravno mesto
                                        HttpStatus status = (HttpStatus) super.getStatusCode();

                                        // Ako nije greška – ne diramo response
                                        if (status == null || !status.isError()) {
                                            return Mono.just(bufferFactory.wrap(content));
                                        }

                                        ApiError apiError = new ApiError(
                                                status,
                                                errorResponse.errorCode(),
                                                exchange.getRequest().getPath().value()
                                        );

                                        byte[] out = objectMapper.writeValueAsBytes(apiError);

                                        originalResponse.getHeaders()
                                                .setContentType(MediaType.APPLICATION_JSON);

                                        return Mono.just(bufferFactory.wrap(out));

                                    } catch (Exception e) {
                                        // Ako nije ErrorResponse JSON – vrati original
                                        return Mono.just(bufferFactory.wrap(content));
                                    }
                                })
                        );
                    }
                };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//
//        ServerHttpResponse originalResponse = exchange.getResponse();
//        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
//
//
//        ServerHttpResponseDecorator decoratedResponse =
//                new ServerHttpResponseDecorator(originalResponse) {
//
//                    @Override
//                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
//
//                        HttpStatus status = (HttpStatus) exchange.getResponse().getStatusCode();
//
//                        if (status != null && status.isError() && body != null) {
//
//                            Flux<? extends DataBuffer> flux = Flux.from(body);
//
//                            return super.writeWith(
//                                    flux.flatMap(dataBuffer -> {
//
//                                        byte[] content = new byte[dataBuffer.readableByteCount()];
//                                        dataBuffer.read(content);
//                                        DataBufferUtils.release(dataBuffer);
//
//                                        try {
//                                            ThinErrorResponse errorResponse =
//                                                    objectMapper.readValue(content, ThinErrorResponse.class);
//
//                                            ApiError apiError = new ApiError(
//                                                    status,
//                                                    errorResponse.errorCode(),
//                                                    exchange.getRequest().getPath().value()
//                                            );
//
//                                            byte[] bytes = objectMapper.writeValueAsBytes(apiError);
//
//                                            originalResponse.getHeaders()
//                                                    .setContentType(MediaType.APPLICATION_JSON);
//
//                                            return Mono.just(bufferFactory.wrap(bytes));
//
//                                        } catch (Exception e) {
//                                            return Mono.error(e);
//                                        }
//                                    })
//                            );
//                        }
//
//                        return super.writeWith(body);
//                    }
//                };
//
//        return chain.filter(exchange.mutate().response(decoratedResponse).build());
//    }


//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        return chain.filter(exchange)
////                .onErrorResume(throwable -> handleError(exchange, throwable));
//
//                .onErrorResume(WebClientResponseException.class, ex -> {
//
//                    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
//                    ServerHttpResponse response = exchange.getResponse();
//
//                    if (response.isCommitted()) {
//                        return Mono.error(ex);
//                    }
//
//                    ApiError apiError = new ApiError(
//                            status,
//                            extractMessage(ex),
//                            exchange.getRequest().getPath().value()
//                    );
//
//
//                    response.setStatusCode(status);
//                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//
//                    try {
//                        byte[] bytes = objectMapper.writeValueAsBytes(apiError);
//                        DataBuffer buffer = response.bufferFactory().wrap(bytes);
//                        return response.writeWith(Mono.just(buffer));
//                    } catch (Exception e) {
//                        return Mono.error(e);
//                    }
//                });
//    }


//    private Mono<Void> handleError(ServerWebExchange exchange, Throwable throwable) {
//
//        log.error("Gateway caught error: {}", throwable.getClass().getName());
//
//        if (throwable instanceof WebClientResponseException ex) {
//            return mapWebClientException(exchange, ex);
//        }
//        // fallback – pustamo Spring default
//        return Mono.error(throwable);
//    }


//    private String extractMessage(WebClientResponseException ex) {
//        try {
//            JsonNode node = objectMapper.readTree(ex.getResponseBodyAsByteArray());
//            return node.path("errorCode").asText("UNKNOWN_ERROR");
//        } catch (Exception e) {
//            return "UNKNOWN_ERROR";
//        }
//    }

//    private Mono<Void> mapWebClientException(
//            ServerWebExchange exchange,
//            WebClientResponseException ex) {
//
//        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
//
//        ApiError apiError = new ApiError(
//                status,
//                extractMessage(ex),
//                exchange.getRequest().getPath().value()
//        );
//
//        byte[] bytes;
//        try {
//            bytes = objectMapper.writeValueAsBytes(apiError);
//        } catch (JsonProcessingException e) {
//            return Mono.error(e);
//        }
//
//        ServerHttpResponse response = exchange.getResponse();
//        response.setStatusCode(status);
//        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//
//        DataBuffer buffer = response.bufferFactory().wrap(bytes);
//        return response.writeWith(Mono.just(buffer));
//    }
//
//    private String extractMessage(WebClientResponseException ex) {
//        return ex.getResponseBodyAsString();
//    }


}
