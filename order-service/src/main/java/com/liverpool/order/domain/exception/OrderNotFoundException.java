package com.liverpool.order.domain.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }

    public static OrderNotFoundException withOrderId(String orderId) {
        return new OrderNotFoundException("Pedido no encontrado con orderId: " + orderId);
    }
}
