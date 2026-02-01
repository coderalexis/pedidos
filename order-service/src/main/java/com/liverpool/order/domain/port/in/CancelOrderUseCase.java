package com.liverpool.order.domain.port.in;

import com.liverpool.order.domain.model.Order;

public interface CancelOrderUseCase {
    Order cancelOrder(String orderId);
}
