package com.liverpool.order.domain.port.in;

import com.liverpool.order.domain.model.Order;
import com.liverpool.order.domain.model.OrderStatus;

public interface UpdateOrderStatusUseCase {
    Order updateOrderStatus(String orderId, OrderStatus newStatus);
}
