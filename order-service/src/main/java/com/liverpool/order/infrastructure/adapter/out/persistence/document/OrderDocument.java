package com.liverpool.order.infrastructure.adapter.out.persistence.document;

import com.liverpool.order.domain.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
@CompoundIndexes({
        @CompoundIndex(name = "customer_status_idx", def = "{'customerId': 1, 'status': 1}"),
        @CompoundIndex(name = "status_fecha_idx", def = "{'status': 1, 'fechaPedido': -1}")
})
public class OrderDocument {

    @Id
    private String id;  // ObjectId de MongoDB (técnico/interno)

    @Indexed(unique = true)
    private String orderId;  // UUID de negocio (público/externo)

    @Indexed
    private String customerId;  // UUID del cliente (referencia a customer-service)

    @Indexed(unique = true)
    private String orderNumber;

    @Indexed
    private OrderStatus status;

    private BigDecimal totalAmount;

    private String notas;

    @Builder.Default
    private List<OrderItemDocument> items = new ArrayList<>();

    @CreatedDate
    @Indexed
    private LocalDateTime fechaPedido;

    @LastModifiedDate
    private LocalDateTime fechaActualizacion;
}
