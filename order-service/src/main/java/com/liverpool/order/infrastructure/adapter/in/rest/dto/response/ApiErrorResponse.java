package com.liverpool.order.infrastructure.adapter.in.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Respuesta de error estándar")
public class ApiErrorResponse {

    @Schema(description = "Indica si la operación fue exitosa (siempre false para errores)", example = "false")
    private boolean success;

    @Schema(description = "Detalles del error")
    private ErrorDetails error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Detalles específicos del error")
    public static class ErrorDetails {

        @Schema(description = "Código de error", example = "ORDER_NOT_FOUND")
        private String code;

        @Schema(description = "Mensaje de error descriptivo", example = "El pedido no existe")
        private String message;

        @Schema(description = "Detalles adicionales del error")
        private List<String> details;

        @Schema(description = "Errores de validación por campo")
        private Map<String, String> fields;

        @Schema(description = "Timestamp del error en formato ISO", example = "2025-01-31T10:30:00Z")
        private String timestamp;

        @Schema(description = "Ruta del endpoint", example = "/order-service/api/v1/orders/123")
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
