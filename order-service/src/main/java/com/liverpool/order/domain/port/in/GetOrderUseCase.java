package com.liverpool.order.domain.port.in;

import com.liverpool.order.domain.model.Order;
import com.liverpool.order.domain.model.OrderStatus;

import java.util.List;

public interface GetOrderUseCase {
    Order getOrderByOrderId(String orderId);

    List<Order> getAllOrders(int page, int size);

    List<Order> getOrdersByCustomerId(String customerId);

    List<Order> getOrdersByStatus(OrderStatus status, int page, int size);
}
