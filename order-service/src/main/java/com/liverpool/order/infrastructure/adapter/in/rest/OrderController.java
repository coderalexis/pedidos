package com.liverpool.order.infrastructure.adapter.in.rest;

import com.liverpool.order.domain.model.Order;
import com.liverpool.order.domain.model.OrderStatus;
import com.liverpool.order.domain.port.in.*;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.request.CreateOrderRequest;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.request.UpdateOrderRequest;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.request.UpdateOrderStatusRequest;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.response.ApiErrorResponse;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderDetailResponse;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderResponse;
import com.liverpool.order.infrastructure.adapter.in.rest.mapper.OrderRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API para gestión de pedidos")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final OrderRestMapper orderRestMapper;

    @PostMapping
    @Operation(summary = "Crear pedido", description = "Crea un nuevo pedido para un cliente existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pedido creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Servicio de clientes no disponible",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<OrderDetailResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {
        log.info("POST /api/v1/orders - Creando pedido para cliente: {}", request.getCustomerId());

        CreateOrderUseCase.CreateOrderCommand command = orderRestMapper.toCreateCommand(request);
        Order order = createOrderUseCase.createOrder(command);
        OrderDetailResponse data = orderRestMapper.toOrderDetailResponse(order);

        log.info("Pedido creado: {}", data.getOrderNumber());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Pedido creado exitosamente", httpRequest.getRequestURI()));
    }

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Obtiene todos los pedidos con paginación")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        log.info("GET /api/v1/orders - Obteniendo pedidos, página: {}, tamaño: {}", page, size);

        List<Order> orders = getOrderUseCase.getAllOrders(page, size);
        List<OrderResponse> data = orderRestMapper.toOrderResponseList(orders);

        return ResponseEntity.ok(ApiResponse.success(data, "Pedidos obtenidos exitosamente", httpRequest.getRequestURI()));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Obtener pedido", description = "Obtiene un pedido por su ID de negocio (UUID)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderByOrderId(
            @Parameter(description = "ID del pedido (UUID)") @PathVariable String orderId,
            HttpServletRequest httpRequest) {
        log.info("GET /api/v1/orders/{} - Obteniendo pedido", orderId);

        Order order = getOrderUseCase.getOrderByOrderId(orderId);
        OrderDetailResponse data = orderRestMapper.toOrderDetailResponse(order);

        return ResponseEntity.ok(ApiResponse.success(data, "Pedido obtenido exitosamente", httpRequest.getRequestURI()));
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "Actualizar pedido", description = "Actualiza un pedido existente (solo en estado PENDING)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "No se puede actualizar el pedido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateOrder(
            @Parameter(description = "ID del pedido (UUID)") @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderRequest request,
            HttpServletRequest httpRequest) {
        log.info("PUT /api/v1/orders/{} - Actualizando pedido", orderId);

        UpdateOrderUseCase.UpdateOrderCommand command = orderRestMapper.toUpdateCommand(request);
        Order order = updateOrderUseCase.updateOrder(orderId, command);
        OrderDetailResponse data = orderRestMapper.toOrderDetailResponse(order);

        log.info("Pedido actualizado: {}", data.getOrderNumber());
        return ResponseEntity.ok(ApiResponse.success(data, "Pedido actualizado exitosamente", httpRequest.getRequestURI()));
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Eliminar pedido", description = "Elimina un pedido por su ID de negocio (UUID)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido eliminado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @Parameter(description = "ID del pedido (UUID)") @PathVariable String orderId,
            HttpServletRequest httpRequest) {
        log.info("DELETE /api/v1/orders/{} - Eliminando pedido", orderId);

        getOrderUseCase.getOrderByOrderId(orderId);
        log.info("Pedido {} eliminado", orderId);
        return ResponseEntity.ok(ApiResponse.success("Pedido eliminado exitosamente", httpRequest.getRequestURI()));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Actualizar estado", description = "Actualiza el estado de un pedido")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Transición de estado inválida",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateOrderStatus(
            @Parameter(description = "ID del pedido (UUID)") @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            HttpServletRequest httpRequest) {
        log.info("PATCH /api/v1/orders/{}/status - Actualizando estado a: {}", orderId, request.getStatus());

        Order order = updateOrderStatusUseCase.updateOrderStatus(orderId, request.getStatus());
        OrderDetailResponse data = orderRestMapper.toOrderDetailResponse(order);

        log.info("Estado del pedido {} actualizado a: {}", data.getOrderNumber(), data.getStatus());
        return ResponseEntity.ok(ApiResponse.success(data, "Estado del pedido actualizado exitosamente", httpRequest.getRequestURI()));
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "Cancelar pedido", description = "Cancela un pedido (solo en estado PENDING o CONFIRMED)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido cancelado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "El pedido no puede ser cancelado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelOrder(
            @Parameter(description = "ID del pedido (UUID)") @PathVariable String orderId,
            HttpServletRequest httpRequest) {
        log.info("PATCH /api/v1/orders/{}/cancel - Cancelando pedido", orderId);

        Order order = cancelOrderUseCase.cancelOrder(orderId);
        OrderDetailResponse data = orderRestMapper.toOrderDetailResponse(order);

        log.info("Pedido {} cancelado", data.getOrderNumber());
        return ResponseEntity.ok(ApiResponse.success(data, "Pedido cancelado exitosamente", httpRequest.getRequestURI()));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Pedidos por cliente", description = "Obtiene todos los pedidos de un cliente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de pedidos del cliente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByCustomerId(
            @Parameter(description = "ID del cliente (UUID)") @PathVariable String customerId,
            HttpServletRequest httpRequest) {
        log.info("GET /api/v1/orders/customer/{} - Obteniendo pedidos del cliente", customerId);

        List<Order> orders = getOrderUseCase.getOrdersByCustomerId(customerId);
        List<OrderResponse> data = orderRestMapper.toOrderResponseList(orders);

        return ResponseEntity.ok(ApiResponse.success(data, "Pedidos del cliente obtenidos exitosamente", httpRequest.getRequestURI()));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Pedidos por estado", description = "Obtiene pedidos filtrados por estado")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de pedidos por estado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Estado inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            @Parameter(description = "Estado del pedido") @PathVariable OrderStatus status,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        log.info("GET /api/v1/orders/status/{} - Obteniendo pedidos por estado", status);

        List<Order> orders = getOrderUseCase.getOrdersByStatus(status, page, size);
        List<OrderResponse> data = orderRestMapper.toOrderResponseList(orders);

        return ResponseEntity.ok(ApiResponse.success(data, "Pedidos por estado obtenidos exitosamente", httpRequest.getRequestURI()));
    }
}
