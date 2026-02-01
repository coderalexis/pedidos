package com.liverpool.order.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para crear un nuevo pedido")
public class CreateOrderRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    @NotBlank(message = "El ID del cliente no puede estar vacío")
    @Schema(description = "ID del cliente que realiza el pedido (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String customerId;

    @NotNull(message = "El pedido debe tener al menos un item")
    @NotEmpty(message = "El pedido debe tener al menos un item")
    @Size(min = 1, max = 50, message = "El pedido debe tener entre 1 y 50 items")
    @Valid
    @Schema(description = "Lista de items del pedido")
    private List<OrderItemRequest> items;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    @Schema(description = "Notas adicionales del pedido", example = "Entregar en horario de oficina")
    private String notas;
}
