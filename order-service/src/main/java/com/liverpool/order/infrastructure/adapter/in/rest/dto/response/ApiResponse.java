package com.liverpool.order.infrastructure.adapter.in.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * @param <T> Tipo de datos contenidos en la respuesta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta estándar de API")
public class ApiResponse<T> {

    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    private boolean success;

    @Schema(description = "Datos de la respuesta")
    private T data;

    @Schema(description = "Mensaje descriptivo de la operación", example = "Operación exitosa")
    private String message;

    @Schema(description = "Timestamp de la respuesta en formato ISO", example = "2025-01-31T10:30:00Z")
    private String timestamp;

    @Schema(description = "Ruta del endpoint", example = "/order-service/api/v1/orders")
    private String path;

    public static <T> ApiResponse<T> success(T data, String message, String path) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now().toString())
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String path) {
        return success(data, "Operación exitosa", path);
    }

    public static <Void> ApiResponse<Void> success(String message, String path) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now().toString())
                .path(path)
                .build();
    }
}
