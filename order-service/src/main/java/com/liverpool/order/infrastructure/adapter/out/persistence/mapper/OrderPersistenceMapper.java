package com.liverpool.order.infrastructure.adapter.out.persistence.mapper;

import com.liverpool.order.domain.model.Order;
import com.liverpool.order.domain.model.OrderItem;
import com.liverpool.order.infrastructure.adapter.out.persistence.document.OrderDocument;
import com.liverpool.order.infrastructure.adapter.out.persistence.document.OrderItemDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderPersistenceMapper {

    @Mapping(target = "id", ignore = true)  // MongoDB genera el _id automáticamente
    OrderDocument toDocument(Order order);

    Order toDomain(OrderDocument document);

    OrderItemDocument toItemDocument(OrderItem item);

    OrderItem toItemDomain(OrderItemDocument document);

    List<OrderItem> toItemDomainList(List<OrderItemDocument> documents);

    List<OrderItemDocument> toItemDocumentList(List<OrderItem> items);

    List<Order> toDomainList(List<OrderDocument> documents);
}
