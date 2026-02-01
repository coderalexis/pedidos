package com.liverpool.order.application.service;

import com.liverpool.order.domain.exception.CustomerNotFoundException;
import com.liverpool.order.domain.exception.OrderCannotBeCancelledException;
import com.liverpool.order.domain.exception.OrderNotFoundException;
import com.liverpool.order.domain.model.Order;
import com.liverpool.order.domain.model.OrderItem;
import com.liverpool.order.domain.model.OrderStatus;
import com.liverpool.order.domain.port.in.CreateOrderUseCase;
import com.liverpool.order.domain.port.in.UpdateOrderUseCase;
import com.liverpool.order.domain.port.out.CustomerValidationPort;
import com.liverpool.order.domain.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private CustomerValidationPort customerValidationPort;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private CreateOrderUseCase.CreateOrderCommand createCommand;

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

        createCommand = CreateOrderUseCase.CreateOrderCommand.builder()
                .customerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
                .items(List.of(
                        CreateOrderUseCase.OrderItemCommand.builder()
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
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully when customer exists")
        void shouldCreateOrderSuccessfully() {
            // Given
            when(customerValidationPort.customerExists("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")).thenReturn(true);
            when(orderRepositoryPort.save(any(Order.class))).thenReturn(testOrder);

            // When
            Order result = orderService.createOrder(createCommand);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f");
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

            verify(customerValidationPort).customerExists("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f");
            verify(orderRepositoryPort).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when customer does not exist")
        void shouldThrowExceptionWhenCustomerNotExists() {
            // Given
            when(customerValidationPort.customerExists("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> orderService.createOrder(createCommand))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining("Cliente no encontrado");

            verify(customerValidationPort).customerExists("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f");
            verify(orderRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should calculate total amount correctly")
        void shouldCalculateTotalAmountCorrectly() {
            // Given
            when(customerValidationPort.customerExists("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")).thenReturn(true);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            when(orderRepositoryPort.save(orderCaptor.capture())).thenReturn(testOrder);

            // When
            orderService.createOrder(createCommand);

            // Then
            Order capturedOrder = orderCaptor.getValue();
            assertThat(capturedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("Should generate unique order number")
        void shouldGenerateUniqueOrderNumber() {
            // Given
            when(customerValidationPort.customerExists("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")).thenReturn(true);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            when(orderRepositoryPort.save(orderCaptor.capture())).thenReturn(testOrder);

            // When
            orderService.createOrder(createCommand);

            // Then
            Order capturedOrder = orderCaptor.getValue();
            assertThat(capturedOrder.getOrderNumber()).startsWith("ORD-");
        }
    }

    @Nested
    @DisplayName("Get Order Tests")
    class GetOrderTests {

        @Test
        @DisplayName("Should return order when found by orderId")
        void shouldReturnOrderWhenFoundByOrderId() {
            // Given
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(testOrder));

            // When
            Order result = orderService.getOrderByOrderId("550e8400-e29b-41d4-a716-446655440000");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
            verify(orderRepositoryPort).findByOrderId("550e8400-e29b-41d4-a716-446655440000");
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            // Given
            when(orderRepositoryPort.findByOrderId(anyString())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> orderService.getOrderByOrderId("non-existent-id"))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("Should return all orders with pagination")
        void shouldReturnAllOrdersWithPagination() {
            // Given
            when(orderRepositoryPort.findAll(0, 10)).thenReturn(List.of(testOrder));

            // When
            List<Order> result = orderService.getAllOrders(0, 10);

            // Then
            assertThat(result).hasSize(1);
            verify(orderRepositoryPort).findAll(0, 10);
        }

        @Test
        @DisplayName("Should return orders by customer ID")
        void shouldReturnOrdersByCustomerId() {
            // Given
            when(orderRepositoryPort.findByCustomerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")).thenReturn(List.of(testOrder));

            // When
            List<Order> result = orderService.getOrdersByCustomerId("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCustomerId()).isEqualTo("c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f");
        }

        @Test
        @DisplayName("Should return orders by status")
        void shouldReturnOrdersByStatus() {
            // Given
            when(orderRepositoryPort.findByStatus(OrderStatus.PENDING, 0, 10))
                    .thenReturn(List.of(testOrder));

            // When
            List<Order> result = orderService.getOrdersByStatus(OrderStatus.PENDING, 0, 10);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Update Order Tests")
    class UpdateOrderTests {

        @Test
        @DisplayName("Should update order successfully when in PENDING status")
        void shouldUpdateOrderSuccessfully() {
            // Given
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(testOrder));
            when(orderRepositoryPort.save(any(Order.class))).thenReturn(testOrder);

            UpdateOrderUseCase.UpdateOrderCommand command = UpdateOrderUseCase.UpdateOrderCommand.builder()
                    .notas("Updated notes")
                    .build();

            // When
            Order result = orderService.updateOrder("550e8400-e29b-41d4-a716-446655440000", command);

            // Then
            assertThat(result).isNotNull();
            verify(orderRepositoryPort).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-pending order")
        void shouldThrowExceptionWhenUpdatingNonPendingOrder() {
            // Given
            testOrder.setStatus(OrderStatus.CONFIRMED);
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(testOrder));

            UpdateOrderUseCase.UpdateOrderCommand command = UpdateOrderUseCase.UpdateOrderCommand.builder()
                    .notas("Updated notes")
                    .build();

            // When/Then
            assertThatThrownBy(() -> orderService.updateOrder("550e8400-e29b-41d4-a716-446655440000", command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Solo se pueden actualizar pedidos en estado PENDING");
        }
    }

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order in PENDING status")
        void shouldCancelOrderInPendingStatus() {
            // Given
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(testOrder));
            when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> {
                Order order = inv.getArgument(0);
                return order;
            });

            // When
            Order result = orderService.cancelOrder("550e8400-e29b-41d4-a716-446655440000");

            // Then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(orderRepositoryPort).save(any(Order.class));
        }

        @Test
        @DisplayName("Should cancel order in CONFIRMED status")
        void shouldCancelOrderInConfirmedStatus() {
            // Given
            testOrder.setStatus(OrderStatus.CONFIRMED);
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(testOrder));
            when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> {
                Order order = inv.getArgument(0);
                return order;
            });

            // When
            Order result = orderService.cancelOrder("550e8400-e29b-41d4-a716-446655440000");

            // Then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should throw exception when cancelling shipped order")
        void shouldThrowExceptionWhenCancellingShippedOrder() {
            // Given
            testOrder.setStatus(OrderStatus.SHIPPED);
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(testOrder));

            // When/Then
            assertThatThrownBy(() -> orderService.cancelOrder("550e8400-e29b-41d4-a716-446655440000"))
                    .isInstanceOf(OrderCannotBeCancelledException.class);
        }
    }

    @Nested
    @DisplayName("Update Order Status Tests")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update status from PENDING to CONFIRMED")
        void shouldUpdateStatusFromPendingToConfirmed() {
            // Given
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(testOrder));
            when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> {
                Order order = inv.getArgument(0);
                return order;
            });

            // When
            Order result = orderService.updateOrderStatus("550e8400-e29b-41d4-a716-446655440000", OrderStatus.CONFIRMED);

            // Then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should follow valid status transitions")
        void shouldFollowValidStatusTransitions() {
            // Given - PENDING order
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(testOrder));
            when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // PENDING -> CONFIRMED
            Order confirmed = orderService.updateOrderStatus("550e8400-e29b-41d4-a716-446655440000", OrderStatus.CONFIRMED);
            assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            // CONFIRMED -> PROCESSING
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(confirmed));
            Order processing = orderService.updateOrderStatus("550e8400-e29b-41d4-a716-446655440000", OrderStatus.PROCESSING);
            assertThat(processing.getStatus()).isEqualTo(OrderStatus.PROCESSING);

            // PROCESSING -> SHIPPED
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(processing));
            Order shipped = orderService.updateOrderStatus("550e8400-e29b-41d4-a716-446655440000", OrderStatus.SHIPPED);
            assertThat(shipped.getStatus()).isEqualTo(OrderStatus.SHIPPED);

            // SHIPPED -> DELIVERED
            when(orderRepositoryPort.findByOrderId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(shipped));
            Order delivered = orderService.updateOrderStatus("550e8400-e29b-41d4-a716-446655440000", OrderStatus.DELIVERED);
            assertThat(delivered.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }
    }
}
