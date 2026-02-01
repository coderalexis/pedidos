package com.liverpool.order.infrastructure.adapter.out.persistence.repository;

import com.liverpool.order.domain.model.OrderStatus;
import com.liverpool.order.infrastructure.adapter.out.persistence.document.OrderDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderMongoRepository extends MongoRepository<OrderDocument, String> {

    Optional<OrderDocument> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    void deleteByOrderId(String orderId);

    List<OrderDocument> findByCustomerIdOrderByFechaPedidoDesc(String customerId);

    Page<OrderDocument> findByStatus(OrderStatus status, Pageable pageable);

    List<OrderDocument> findByCustomerIdAndStatus(String customerId, OrderStatus status);

    Optional<OrderDocument> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);
}
