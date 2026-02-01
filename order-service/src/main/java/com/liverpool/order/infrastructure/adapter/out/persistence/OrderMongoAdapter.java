package com.liverpool.order.infrastructure.adapter.out.persistence;

import com.liverpool.order.domain.model.Order;
import com.liverpool.order.domain.model.OrderStatus;
import com.liverpool.order.domain.port.out.OrderRepositoryPort;
import com.liverpool.order.infrastructure.adapter.out.persistence.document.OrderDocument;
import com.liverpool.order.infrastructure.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.liverpool.order.infrastructure.adapter.out.persistence.repository.OrderMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMongoAdapter implements OrderRepositoryPort {

    private final OrderMongoRepository orderMongoRepository;
    private final OrderPersistenceMapper orderPersistenceMapper;

    @Override
    public Order save(Order order) {
        log.debug("Guardando pedido: {}", order.getOrderNumber());

        OrderDocument document = orderPersistenceMapper.toDocument(order);

        // Si es una actualización, buscar el documento existente para preservar el _id de MongoDB
        if (order.getOrderId() != null) {
            orderMongoRepository.findByOrderId(order.getOrderId())
                    .ifPresent(existingDoc -> document.setId(existingDoc.getId()));
        }

        OrderDocument savedDocument = orderMongoRepository.save(document);

        log.debug("Pedido guardado con orderId: {}", savedDocument.getOrderId());
        return orderPersistenceMapper.toDomain(savedDocument);
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        log.debug("Buscando pedido por orderId: {}", orderId);
        return orderMongoRepository.findByOrderId(orderId)
                .map(orderPersistenceMapper::toDomain);
    }

    @Override
    public List<Order> findAll(int page, int size) {
        log.debug("Obteniendo todos los pedidos, página: {}, tamaño: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaPedido"));
        return orderPersistenceMapper.toDomainList(
                orderMongoRepository.findAll(pageRequest).getContent()
        );
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        log.debug("Buscando pedidos del cliente: {}", customerId);
        return orderPersistenceMapper.toDomainList(
                orderMongoRepository.findByCustomerIdOrderByFechaPedidoDesc(customerId)
        );
    }

    @Override
    public List<Order> findByStatus(OrderStatus status, int page, int size) {
        log.debug("Buscando pedidos por estado: {}", status);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaPedido"));
        return orderPersistenceMapper.toDomainList(
                orderMongoRepository.findByStatus(status, pageRequest).getContent()
        );
    }

    @Override
    public void deleteByOrderId(String orderId) {
        log.debug("Eliminando pedido con orderId: {}", orderId);
        orderMongoRepository.deleteByOrderId(orderId);
    }

    @Override
    public boolean existsByOrderId(String orderId) {
        return orderMongoRepository.existsByOrderId(orderId);
    }

    @Override
    public long count() {
        return orderMongoRepository.count();
    }
}
