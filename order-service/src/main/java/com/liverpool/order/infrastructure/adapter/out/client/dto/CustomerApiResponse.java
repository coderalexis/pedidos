package com.liverpool.order.infrastructure.adapter.out.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @param <T> Tipo de datos contenidos en la respuesta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String timestamp;
    private String path;
}
