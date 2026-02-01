package com.liverpool.order.infrastructure.adapter.in.rest.dto.request;

import com.liverpool.order.domain.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para actualizar el estado de un pedido")
public class UpdateOrderStatusRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    @Schema(description = "Nuevo estado del pedido", example = "CONFIRMED")
    private OrderStatus status;
}
