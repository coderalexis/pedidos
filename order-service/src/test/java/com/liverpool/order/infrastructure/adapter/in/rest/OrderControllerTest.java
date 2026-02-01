package com.liverpool.order.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liverpool.order.domain.exception.CustomerNotFoundException;
import com.liverpool.order.domain.exception.CustomerServiceUnavailableException;
import com.liverpool.order.domain.exception.OrderNotFoundException;
import com.liverpool.order.domain.model.Order;
import com.liverpool.order.domain.model.OrderItem;
import com.liverpool.order.domain.model.OrderStatus;
import com.liverpool.order.domain.port.in.*;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.request.CreateOrderRequest;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.request.OrderItemRequest;
import com.liverpool.order.infrastructure.adapter.in.rest.dto.request.UpdateOrderStatusRequest;
import com.liverpool.order.infrastructure.adapter.in.rest.mapper.OrderRestMapper;
import com.liverpool.order.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import({GlobalExceptionHandler.class})
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;

    @MockBean
    private GetOrderUseCase getOrderUseCase;

    @MockBean
    private UpdateOrderUseCase updateOrderUseCase;

    @MockBean
    private CancelOrderUseCase cancelOrderUseCase;

    @MockBean
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @MockBean
    private OrderRestMapper orderRestMapper;

    private Order testOrder;
    private CreateOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        OrderItem item = OrderItem.builder()
                .id("item-001")
                .codigoProducto("PROD-001")
                .nombreProducto("Test Product")
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("200.00"))
                .build();

        testOrder = Order.builder()
                .orderId("550e8400-e29b-41d4-a716-446655440000")
                .customerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
                .orderNumber("ORD-20240115120000-A1B2C3D4")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("200.00"))
                .items(new ArrayList<>(List.of(item)))
                .fechaPedido(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        createRequest = CreateOrderRequest.builder()
                .customerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
                .items(List.of(
                        OrderItemRequest.builder()
                                .codigoProducto("PROD-001")
                                .nombreProducto("Test Product")
                                .cantidad(2)
                                .precioUnitario(new BigDecimal("100.00"))
                                .build()
                ))
                .notas("Test order")
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/orders")
    class CreateOrderEndpoint {

        @Test
        @DisplayName("Should create order successfully")
        void shouldCreateOrderSuccessfully() throws Exception {
            // Given
            when(orderRestMapper.toCreateCommand(any())).thenReturn(
                    CreateOrderUseCase.CreateOrderCommand.builder()
                            .customerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
                            .items(List.of())
                            .build()
            );
            when(createOrderUseCase.createOrder(any())).thenReturn(testOrder);
            when(orderRestMapper.toOrderDetailResponse(any())).thenReturn(
                    com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderDetailResponse.builder()
                            .orderId("550e8400-e29b-41d4-a716-446655440000")
                            .customerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
                            .orderNumber("ORD-20240115120000-A1B2C3D4")
                            .status(OrderStatus.PENDING)
                            .statusDisplayName("Pendiente")
                            .totalAmount(new BigDecimal("200.00"))
                            .build()
            );

            // When/Then
            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").value("550e8400-e29b-41d4-a716-446655440000"))
                    .andExpect(jsonPath("$.data.customerId").value("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.message").value("Pedido creado exitosamente"));
        }

        @Test
        @DisplayName("Should return 400 when customer ID is null")
        void shouldReturn400WhenCustomerIdIsNull() throws Exception {
            // Given
            createRequest.setCustomerId(null);

            // When/Then
            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 400 when items list is empty")
        void shouldReturn400WhenItemsListIsEmpty() throws Exception {
            // Given
            createRequest.setItems(List.of());

            // When/Then
            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 404 when customer not found")
        void shouldReturn404WhenCustomerNotFound() throws Exception {
            // Given
            when(orderRestMapper.toCreateCommand(any())).thenReturn(
                    CreateOrderUseCase.CreateOrderCommand.builder()
                            .customerId("99999999-9999-9999-9999-999999999999")
                            .items(List.of())
                            .build()
            );
            when(createOrderUseCase.createOrder(any()))
                    .thenThrow(new CustomerNotFoundException("Cliente no encontrado: 99999999-9999-9999-9999-999999999999"));

            // When/Then
            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CUSTOMER_NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 503 when customer service unavailable")
        void shouldReturn503WhenCustomerServiceUnavailable() throws Exception {
            // Given
            when(orderRestMapper.toCreateCommand(any())).thenReturn(
                    CreateOrderUseCase.CreateOrderCommand.builder()
                            .customerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
                            .items(List.of())
                            .build()
            );
            when(createOrderUseCase.createOrder(any()))
                    .thenThrow(new CustomerServiceUnavailableException("Service unavailable"));

            // When/Then
            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("SERVICE_UNAVAILABLE"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{orderId}")
    class GetOrderByOrderIdEndpoint {

        @Test
        @DisplayName("Should return order when found")
        void shouldReturnOrderWhenFound() throws Exception {
            // Given
            when(getOrderUseCase.getOrderByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(testOrder);
            when(orderRestMapper.toOrderDetailResponse(any())).thenReturn(
                    com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderDetailResponse.builder()
                            .orderId("550e8400-e29b-41d4-a716-446655440000")
                            .customerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
                            .orderNumber("ORD-20240115120000-A1B2C3D4")
                            .status(OrderStatus.PENDING)
                            .statusDisplayName("Pendiente")
                            .totalAmount(new BigDecimal("200.00"))
                            .build()
            );

            // When/Then
            mockMvc.perform(get("/api/v1/orders/550e8400-e29b-41d4-a716-446655440000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").value("550e8400-e29b-41d4-a716-446655440000"))
                    .andExpect(jsonPath("$.data.orderNumber").value("ORD-20240115120000-A1B2C3D4"));
        }

        @Test
        @DisplayName("Should return 404 when order not found")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            // Given
            when(getOrderUseCase.getOrderByOrderId(anyString()))
                    .thenThrow(OrderNotFoundException.withOrderId("non-existent-id"));

            // When/Then
            mockMvc.perform(get("/api/v1/orders/non-existent-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("ORDER_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders")
    class GetAllOrdersEndpoint {

        @Test
        @DisplayName("Should return paginated orders")
        void shouldReturnPaginatedOrders() throws Exception {
            // Given
            when(getOrderUseCase.getAllOrders(0, 20)).thenReturn(List.of(testOrder));
            when(orderRestMapper.toOrderResponseList(any())).thenReturn(List.of(
                    com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderResponse.builder()
                            .orderId("550e8400-e29b-41d4-a716-446655440000")
                            .customerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
                            .status(OrderStatus.PENDING)
                            .build()
            ));

            // When/Then
            mockMvc.perform(get("/api/v1/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/orders/{orderId}/status")
    class UpdateOrderStatusEndpoint {

        @Test
        @DisplayName("Should update order status successfully")
        void shouldUpdateOrderStatusSuccessfully() throws Exception {
            // Given
            UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                    .status(OrderStatus.CONFIRMED)
                    .build();

            testOrder.setStatus(OrderStatus.CONFIRMED);
            when(updateOrderStatusUseCase.updateOrderStatus("550e8400-e29b-41d4-a716-446655440000", OrderStatus.CONFIRMED))
                    .thenReturn(testOrder);
            when(orderRestMapper.toOrderDetailResponse(any())).thenReturn(
                    com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderDetailResponse.builder()
                            .orderId("550e8400-e29b-41d4-a716-446655440000")
                            .status(OrderStatus.CONFIRMED)
                            .statusDisplayName("Confirmado")
                            .build()
            );

            // When/Then
            mockMvc.perform(patch("/api/v1/orders/550e8400-e29b-41d4-a716-446655440000/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/orders/{orderId}/cancel")
    class CancelOrderEndpoint {

        @Test
        @DisplayName("Should cancel order successfully")
        void shouldCancelOrderSuccessfully() throws Exception {
            // Given
            testOrder.setStatus(OrderStatus.CANCELLED);
            when(cancelOrderUseCase.cancelOrder("550e8400-e29b-41d4-a716-446655440000")).thenReturn(testOrder);
            when(orderRestMapper.toOrderDetailResponse(any())).thenReturn(
                    com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderDetailResponse.builder()
                            .orderId("550e8400-e29b-41d4-a716-446655440000")
                            .status(OrderStatus.CANCELLED)
                            .statusDisplayName("Cancelado")
                            .build()
            );

            // When/Then
            mockMvc.perform(patch("/api/v1/orders/550e8400-e29b-41d4-a716-446655440000/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/customer/{customerId}")
    class GetOrdersByCustomerEndpoint {

        @Test
        @DisplayName("Should return orders for customer")
        void shouldReturnOrdersForCustomer() throws Exception {
            // Given
            when(getOrderUseCase.getOrdersByCustomerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")).thenReturn(List.of(testOrder));
            when(orderRestMapper.toOrderResponseList(any())).thenReturn(List.of(
                    com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderResponse.builder()
                            .orderId("550e8400-e29b-41d4-a716-446655440000")
                            .customerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
                            .status(OrderStatus.PENDING)
                            .build()
            ));

            // When/Then
            mockMvc.perform(get("/api/v1/orders/customer/c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].customerId").value("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/status/{status}")
    class GetOrdersByStatusEndpoint {

        @Test
        @DisplayName("Should return orders by status")
        void shouldReturnOrdersByStatus() throws Exception {
            // Given
            when(getOrderUseCase.getOrdersByStatus(OrderStatus.PENDING, 0, 20))
                    .thenReturn(List.of(testOrder));
            when(orderRestMapper.toOrderResponseList(any())).thenReturn(List.of(
                    com.liverpool.order.infrastructure.adapter.in.rest.dto.response.OrderResponse.builder()
                            .orderId("550e8400-e29b-41d4-a716-446655440000")
                            .status(OrderStatus.PENDING)
                            .build()
            ));

            // When/Then
            mockMvc.perform(get("/api/v1/orders/status/PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }
    }
}
