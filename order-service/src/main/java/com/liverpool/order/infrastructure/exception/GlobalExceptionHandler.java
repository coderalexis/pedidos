package com.liverpool.order.infrastructure.exception;

import com.liverpool.order.domain.exception.*;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotFoundException(
            OrderNotFoundException ex, HttpServletRequest request) {
        log.warn("Pedido no encontrado: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "ORDER_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerNotFoundException(
            CustomerNotFoundException ex, HttpServletRequest request) {
        log.warn("Cliente no encontrado: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "CUSTOMER_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CustomerServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerServiceUnavailableException(
            CustomerServiceUnavailableException ex, HttpServletRequest request) {
        log.error("Servicio de clientes no disponible: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "SERVICE_UNAVAILABLE",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOrderStatusException(
            InvalidOrderStatusException ex, HttpServletRequest request) {
        log.warn("Transición de estado inválida: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "INVALID_STATUS_TRANSITION",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OrderCannotBeCancelledException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderCannotBeCancelledException(
            OrderCannotBeCancelledException ex, HttpServletRequest request) {
        log.warn("Pedido no puede ser cancelado: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "ORDER_CANNOT_BE_CANCELLED",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        log.warn("Estado ilegal: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "ILLEGAL_STATE",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Error de validación: {}", ex.getMessage());

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ApiErrorResponse error = ApiErrorResponse.validationError(fieldErrors, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Error de tipo de argumento: {}", ex.getMessage());

        String message = String.format("El parámetro '%s' tiene un valor inválido: '%s'",
                ex.getName(), ex.getValue());

        ApiErrorResponse error = ApiErrorResponse.of(
                "TYPE_MISMATCH",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Error al leer mensaje HTTP: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "MALFORMED_REQUEST",
                "El cuerpo de la solicitud es inválido o está malformado",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Error interno no manejado: ", ex);

        ApiErrorResponse error = ApiErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "Error interno del servidor. Por favor, contacte al administrador.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
