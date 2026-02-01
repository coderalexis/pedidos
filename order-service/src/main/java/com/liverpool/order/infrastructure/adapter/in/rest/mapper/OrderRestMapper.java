package com.liverpool.order.infrastructure.adapter.in.rest.mapper;

import com.liverpool.order.domain.model.Order;
import com.liverpool.order.domain.model.OrderItem;
import com.liverpool.order.domain.port.in.CreateOrderUseCase;
import com.liverpool.order.domain.port.in.UpdateOrderUseCase;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.request.CreateOrderRequest;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.request.OrderItemRequest;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.request.UpdateOrderRequest;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderDetailResponse;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderItemResponse;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderRestMapper {

    @Mapping(target = "items", source = "items", qualifiedByName = "toItemCommands")
    CreateOrderUseCase.CreateOrderCommand toCreateCommand(CreateOrderRequest request);

    @Mapping(target = "items", source = "items", qualifiedByName = "toUpdateItemCommands")
    UpdateOrderUseCase.UpdateOrderCommand toUpdateCommand(UpdateOrderRequest request);

    @Named("toItemCommands")
    default List<CreateOrderUseCase.OrderItemCommand> toItemCommands(List<OrderItemRequest> items) {
        if (items == null) return null;
        return items.stream()
                .map(this::toItemCommand)
                .toList();
    }

    default CreateOrderUseCase.OrderItemCommand toItemCommand(OrderItemRequest request) {
        return CreateOrderUseCase.OrderItemCommand.builder()
                .codigoProducto(request.getCodigoProducto())
                .nombreProducto(request.getNombreProducto())
                .cantidad(request.getCantidad())
                .precioUnitario(request.getPrecioUnitario())
                .build();
    }

    @Named("toUpdateItemCommands")
    default List<UpdateOrderUseCase.UpdateOrderItemCommand> toUpdateItemCommands(List<OrderItemRequest> items) {
        if (items == null) return null;
        return items.stream()
                .map(this::toUpdateItemCommand)
                .toList();
    }

    default UpdateOrderUseCase.UpdateOrderItemCommand toUpdateItemCommand(OrderItemRequest request) {
        return UpdateOrderUseCase.UpdateOrderItemCommand.builder()
                .codigoProducto(request.getCodigoProducto())
                .nombreProducto(request.getNombreProducto())
                .cantidad(request.getCantidad())
                .precioUnitario(request.getPrecioUnitario())
                .build();
    }

    @Mapping(target = "statusDisplayName", expression = "java(order.getStatus().getDisplayName())")
    @Mapping(target = "itemCount", expression = "java(order.getItems() != null ? order.getItems().size() : 0)")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "statusDisplayName", expression = "java(order.getStatus().getDisplayName())")
    @Mapping(target = "items", source = "items")
    OrderDetailResponse toOrderDetailResponse(Order order);

    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderResponse> toOrderResponseList(List<Order> orders);

    List<OrderItemResponse> toItemResponseList(List<OrderItem> items);
}
