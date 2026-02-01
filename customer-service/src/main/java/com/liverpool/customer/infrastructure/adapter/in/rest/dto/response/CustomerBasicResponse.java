package com.liverpool.customer.infrastructure.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBasicResponse {
    private String customerId;
    private String nombreCompleto;
    private String email;
    private AddressResponse direccionEnvio;
}
