package com.liverpool.order.infrastructure.adapter.in.rest.dto.response;

import com.liverpool.order.domain.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta resumida del pedido")
public class OrderResponse {

    @Schema(description = "ID del pedido (UUID de negocio)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String orderId;

    @Schema(description = "ID del cliente (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String customerId;

    @Schema(description = "Número de pedido", example = "ORD-20240115120000-A1B2C3D4")
    private String orderNumber;

    @Schema(description = "Estado del pedido", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Nombre del estado", example = "Pendiente")
    private String statusDisplayName;

    @Schema(description = "Monto total del pedido", example = "31999.98")
    private BigDecimal totalAmount;

    @Schema(description = "Cantidad de items", example = "3")
    private Integer itemCount;

    @Schema(description = "Fecha del pedido")
    private LocalDateTime fechaPedido;

    @Schema(description = "Fecha de última actualización")
    private LocalDateTime fechaActualizacion;
}
