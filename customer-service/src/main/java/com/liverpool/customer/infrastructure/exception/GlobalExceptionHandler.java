package com.liverpool.customer.infrastructure.exception;

import com.liverpool.customer.domain.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.domain.exception.CustomerNotFoundException;
import com.liverpool.customer.domain.exception.InvalidCustomerDataException;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerNotFound(
            CustomerNotFoundException ex,
            HttpServletRequest request) {
        log.warn("Cliente no encontrado: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "CUSTOMER_NOT_FOUND",
                ex.getMessage(),
                List.of("Verifique que el ID del cliente sea correcto"),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerAlreadyExists(
            CustomerAlreadyExistsException ex,
            HttpServletRequest request) {
        log.warn("Cliente duplicado: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "CUSTOMER_ALREADY_EXISTS",
                ex.getMessage(),
                List.of("Ya existe un cliente registrado con este email"),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidCustomerDataException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCustomerData(
            InvalidCustomerDataException ex,
            HttpServletRequest request) {
        log.warn("Datos de cliente inválidos: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "INVALID_CUSTOMER_DATA",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        log.warn("Error de validación en la petición");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ApiErrorResponse error = ApiErrorResponse.validationError(
                fieldErrors,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        log.warn("Argumento ilegal: {}", ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Error inesperado: ", ex);

        ApiErrorResponse error = ApiErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "Ha ocurrido un error inesperado. Por favor, contacte al administrador.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
