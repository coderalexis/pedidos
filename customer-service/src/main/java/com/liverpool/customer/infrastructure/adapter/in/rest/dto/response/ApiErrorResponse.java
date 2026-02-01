package com.liverpool.customer.infrastructure.adapter.in.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private boolean success;
    private ErrorDetails error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String code;
        private String message;
        private List<String> details;
        private Map<String, String> fields;
        private String timestamp;
        private String path;
    }

    public static ApiErrorResponse of(String code, String message, String path) {
        return ApiErrorResponse.builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .timestamp(Instant.now().toString())
                        .path(path)
                        .build())
                .build();
    }

    public static ApiErrorResponse of(String code, String message, List<String> details, String path) {
        return ApiErrorResponse.builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .timestamp(Instant.now().toString())
                        .path(path)
                        .build())
                .build();
    }

    public static ApiErrorResponse validationError(Map<String, String> fieldErrors, String path) {
        return ApiErrorResponse.builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .code("VALIDATION_ERROR")
                        .message("Errores de validación en la solicitud")
                        .fields(fieldErrors)
                        .timestamp(Instant.now().toString())
                        .path(path)
                        .build())
                .build();
    }
}
