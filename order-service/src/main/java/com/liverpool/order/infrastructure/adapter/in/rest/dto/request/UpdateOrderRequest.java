package com.liverpool.order.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = "Request para actualizar un pedido existente")
public class UpdateOrderRequest {

    @Valid
    @Size(max = 50, message = "El pedido no puede tener más de 50 items")
    @Schema(description = "Nueva lista de items del pedido (opcional)")
    private List<OrderItemRequest> items;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    @Schema(description = "Notas adicionales del pedido", example = "Actualizar dirección de entrega")
    private String notas;
}
