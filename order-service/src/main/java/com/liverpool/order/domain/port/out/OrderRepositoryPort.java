package com.liverpool.order.domain.port.out;

import com.liverpool.order.domain.model.Order;
import com.liverpool.order.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {
    Order save(Order order);

    Optional<Order> findByOrderId(String orderId);

    List<Order> findAll(int page, int size);

    List<Order> findByCustomerId(String customerId);

    List<Order> findByStatus(OrderStatus status, int page, int size);

    void deleteByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    long count();
}
