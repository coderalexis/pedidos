package com.liverpool.order.domain.model;

import com.liverpool.order.domain.exception.InvalidOrderStatusException;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;  // UUID de negocio - identificador público
    private String customerId;  // UUID del cliente (referencia a customer-service)
    private String orderNumber;

    @Setter
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Setter
    private BigDecimal totalAmount;

    @Setter
    private OrderStatus status;

    @Setter
    private String notas;

    private LocalDateTime fechaPedido;

    @Setter
    private LocalDateTime fechaActualizacion;

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        this.totalAmount = calculateTotal();
    }

    public BigDecimal calculateTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public void changeStatus(OrderStatus newStatus) {
        if (!isValidTransition(newStatus)) {
            throw new InvalidOrderStatusException(
                    "No se puede cambiar de " + status.getDisplayName() + " a " + newStatus.getDisplayName()
            );
        }
        this.status = newStatus;
        this.fechaActualizacion = LocalDateTime.now();
    }

    private boolean isValidTransition(OrderStatus newStatus) {
        if (status == null) {
            return true;
        }
        return switch (status) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED ||
                    newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.PROCESSING ||
                    newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
