package com.liverpool.order.infrastructure.adapter.out.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerExistsResponse {
    private String customerId;  // UUID del cliente
    private boolean exists;
    private String message;
}
