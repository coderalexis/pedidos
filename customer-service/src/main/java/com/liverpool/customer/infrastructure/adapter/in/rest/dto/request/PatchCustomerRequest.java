package com.liverpool.customer.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchCustomerRequest {

    @Email(message = "El email debe ser válido")
    private String email;

    @Valid
    private PatchAddressRequest direccionEnvio;
}
