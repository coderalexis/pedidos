package com.liverpool.order.domain.exception;

public class OrderCannotBeCancelledException extends RuntimeException {
    public OrderCannotBeCancelledException(String message) {
        super(message);
    }

    public OrderCannotBeCancelledException(String orderId, String currentStatus) {
        super("El pedido " + orderId + " no puede ser cancelado. Estado actual: " + currentStatus);
    }
}
