package com.liverpool.customer.infrastructure.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private String customerId;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String email;
    private LocalDateTime fechaCreacion;
}
