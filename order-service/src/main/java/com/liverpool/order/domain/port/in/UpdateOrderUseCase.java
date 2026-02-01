package com.liverpool.order.domain.port.in;

import com.liverpool.order.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

public interface UpdateOrderUseCase {
    Order updateOrder(String orderId, UpdateOrderCommand command);

    @Getter
    @Builder
    class UpdateOrderCommand {
        private final List<UpdateOrderItemCommand> items;
        private final String notas;
    }

    @Getter
    @Builder
    class UpdateOrderItemCommand {
        private final String codigoProducto;
        private final String nombreProducto;
        private final Integer cantidad;
        private final BigDecimal precioUnitario;
    }
}
