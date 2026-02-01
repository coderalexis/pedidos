package com.liverpool.order.infrastructure.config;

import com.liverpool.order.application.service.OrderService;
import com.liverpool.order.domain.port.in.*;
import com.liverpool.order.domain.port.out.CustomerValidationPort;
import com.liverpool.order.domain.port.out.OrderRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public OrderService orderService(
            OrderRepositoryPort orderRepositoryPort,
            CustomerValidationPort customerValidationPort) {
        return new OrderService(orderRepositoryPort, customerValidationPort);
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderService orderService) {
        return orderService;
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(OrderService orderService) {
        return orderService;
    }

    @Bean
    public UpdateOrderUseCase updateOrderUseCase(OrderService orderService) {
        return orderService;
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase(OrderService orderService) {
        return orderService;
    }

    @Bean
    public UpdateOrderStatusUseCase updateOrderStatusUseCase(OrderService orderService) {
        return orderService;
    }
}
