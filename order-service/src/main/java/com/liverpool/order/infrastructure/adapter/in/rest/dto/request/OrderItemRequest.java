package com.liverpool.order.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item del pedido")
public class OrderItemRequest {

    @NotBlank(message = "El código del producto es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    @Schema(description = "Código único del producto", example = "PROD-001")
    private String codigoProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Schema(description = "Nombre del producto", example = "Laptop HP Pavilion 15")
    private String nombreProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 9999, message = "La cantidad no puede exceder 9999")
    @Schema(description = "Cantidad del producto", example = "2")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "9999999.99", message = "El precio excede el límite permitido")
    @Digits(integer = 7, fraction = 2, message = "Formato de precio inválido")
    @Schema(description = "Precio unitario del producto", example = "15999.99")
    private BigDecimal precioUnitario;
}
