package com.liverpool.order.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de item del pedido")
public class OrderItemResponse {

    @Schema(description = "ID del item", example = "507f1f77bcf86cd799439012")
    private String id;

    @Schema(description = "Código del producto", example = "PROD-001")
    private String codigoProducto;

    @Schema(description = "Nombre del producto", example = "Laptop HP Pavilion 15")
    private String nombreProducto;

    @Schema(description = "Cantidad", example = "2")
    private Integer cantidad;

    @Schema(description = "Precio unitario", example = "15999.99")
    private BigDecimal precioUnitario;

    @Schema(description = "Subtotal del item", example = "31999.98")
    private BigDecimal subtotal;
}
