package it.eng.auth_service.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import it.eng.auth_service.dto.ErrorResponse;
import it.eng.auth_service.util.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException ex, HttpServletRequest request) {
//        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
            return new ErrorResponse("NOT_FOUND");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleBadCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
//        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
        return new ErrorResponse("INVALID_CREDENTIALS");
    }

//    @ExceptionHandler(ResponseStatusException.class)
//    public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
//        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
//        return ResponseEntity
//                .status(ex.getStatusCode())
//                .body(apiError);
//    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailConflict(EmailAlreadyExistException ex, HttpServletRequest request) {
//        ApiError apiError = new ApiError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
        return new ErrorResponse("EMAIL_ALREADY_EXISTS");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleExpiredJwt(ExpiredJwtException ex, HttpServletRequest request) {
//        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body(apiError);
        return new ErrorResponse("JWT_EXPIRED");
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleJwt(JwtException ex, HttpServletRequest request) {
//        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body(apiError);
        return new ErrorResponse("JWT_INVALID");
    }


}
