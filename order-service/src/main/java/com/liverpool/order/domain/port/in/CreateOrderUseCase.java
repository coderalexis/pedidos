package com.liverpool.order.domain.port.in;

import com.liverpool.order.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

public interface CreateOrderUseCase {
    Order createOrder(CreateOrderCommand command);

    @Getter
    @Builder
    class CreateOrderCommand {
        private final String customerId;  // UUID del cliente
        private final List<OrderItemCommand> items;
        private final String notas;
    }

    @Getter
    @Builder
    class OrderItemCommand {
        private final String codigoProducto;
        private final String nombreProducto;
        private final Integer cantidad;
        private final BigDecimal precioUnitario;
    }
}
