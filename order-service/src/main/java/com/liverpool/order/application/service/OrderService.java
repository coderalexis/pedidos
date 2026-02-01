package com.liverpool.order.application.service;

import com.liverpool.order.domain.exception.CustomerNotFoundException;
import com.liverpool.order.domain.exception.OrderCannotBeCancelledException;
import com.liverpool.order.domain.exception.OrderNotFoundException;
import com.liverpool.order.domain.model.Order;
import com.liverpool.order.domain.model.OrderItem;
import com.liverpool.order.domain.model.OrderStatus;
import com.liverpool.order.domain.port.in.*;
import com.liverpool.order.domain.port.out.CustomerValidationPort;
import com.liverpool.order.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class OrderService implements CreateOrderUseCase, GetOrderUseCase,
        UpdateOrderUseCase, CancelOrderUseCase, UpdateOrderStatusUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final CustomerValidationPort customerValidationPort;

    @Override
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        log.info("Creando pedido para cliente: {}", command.getCustomerId());

        if (!customerValidationPort.customerExists(command.getCustomerId())) {
            throw new CustomerNotFoundException(
                    "No se puede crear el pedido. Cliente no encontrado: " + command.getCustomerId()
            );
        }

        // Generar UUID de negocio para el pedido
        String orderId = UUID.randomUUID().toString();

        Order order = Order.builder()
                .orderId(orderId)
                .customerId(command.getCustomerId())
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .fechaPedido(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .notas(command.getNotas())
                .items(new ArrayList<>())
                .totalAmount(java.math.BigDecimal.ZERO)
                .build();

        command.getItems().forEach(itemCmd -> {
            OrderItem item = new OrderItem(
                    itemCmd.getCodigoProducto(),
                    itemCmd.getNombreProducto(),
                    itemCmd.getCantidad(),
                    itemCmd.getPrecioUnitario()
            );
            order.addItem(item);
        });

        Order savedOrder = orderRepositoryPort.save(order);
        log.info("Pedido creado exitosamente: {}", savedOrder.getOrderNumber());

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByOrderId(String orderId) {
        log.debug("Buscando pedido con orderId: {}", orderId);
        return orderRepositoryPort.findByOrderId(orderId)
                .orElseThrow(() -> OrderNotFoundException.withOrderId(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders(int page, int size) {
        log.debug("Obteniendo todos los pedidos, página: {}, tamaño: {}", page, size);
        return orderRepositoryPort.findAll(page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(String customerId) {
        log.debug("Obteniendo pedidos del cliente: {}", customerId);
        return orderRepositoryPort.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status, int page, int size) {
        log.debug("Obteniendo pedidos con estado: {}", status);
        return orderRepositoryPort.findByStatus(status, page, size);
    }

    @Override
    @Transactional
    public Order updateOrder(String orderId, UpdateOrderCommand command) {
        log.info("Actualizando pedido: {}", orderId);

        Order order = orderRepositoryPort.findByOrderId(orderId)
                .orElseThrow(() -> OrderNotFoundException.withOrderId(orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Solo se pueden actualizar pedidos en estado PENDING. Estado actual: " +
                            order.getStatus().getDisplayName()
            );
        }

        if (command.getNotas() != null) {
            order.setNotas(command.getNotas());
        }

        if (command.getItems() != null && !command.getItems().isEmpty()) {
            List<OrderItem> newItems = new ArrayList<>();
            command.getItems().forEach(itemCmd -> {
                OrderItem item = new OrderItem(
                        itemCmd.getCodigoProducto(),
                        itemCmd.getNombreProducto(),
                        itemCmd.getCantidad(),
                        itemCmd.getPrecioUnitario()
                );
                newItems.add(item);
            });
            order.setItems(newItems);
            order.setTotalAmount(order.calculateTotal());
        }

        order.setFechaActualizacion(LocalDateTime.now());

        Order updatedOrder = orderRepositoryPort.save(order);
        log.info("Pedido actualizado exitosamente: {}", updatedOrder.getOrderNumber());

        return updatedOrder;
    }

    @Override
    @Transactional
    public Order cancelOrder(String orderId) {
        log.info("Cancelando pedido: {}", orderId);

        Order order = orderRepositoryPort.findByOrderId(orderId)
                .orElseThrow(() -> OrderNotFoundException.withOrderId(orderId));

        if (!order.canBeCancelled()) {
            throw new OrderCannotBeCancelledException(orderId, order.getStatus().getDisplayName());
        }

        order.changeStatus(OrderStatus.CANCELLED);
        order.setFechaActualizacion(LocalDateTime.now());

        Order cancelledOrder = orderRepositoryPort.save(order);
        log.info("Pedido cancelado exitosamente: {}", cancelledOrder.getOrderNumber());

        return cancelledOrder;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        log.info("Actualizando estado del pedido {} a {}", orderId, newStatus);

        Order order = orderRepositoryPort.findByOrderId(orderId)
                .orElseThrow(() -> OrderNotFoundException.withOrderId(orderId));

        order.changeStatus(newStatus);
        order.setFechaActualizacion(LocalDateTime.now());

        Order updatedOrder = orderRepositoryPort.save(order);
        log.info("Estado del pedido actualizado exitosamente: {} -> {}",
                updatedOrder.getOrderNumber(), newStatus.getDisplayName());

        return updatedOrder;
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + random;
    }
}
