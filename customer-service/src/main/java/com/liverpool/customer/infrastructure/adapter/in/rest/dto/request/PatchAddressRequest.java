package com.liverpool.customer.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchAddressRequest {

    @Size(max = 100, message = "La calle no puede exceder 100 caracteres")
    private String calle;

    @Size(max = 10, message = "El número exterior no puede exceder 10 caracteres")
    private String numeroExterior;

    @Size(max = 10, message = "El número interior no puede exceder 10 caracteres")
    private String numeroInterior;

    @Size(max = 50, message = "La colonia no puede exceder 50 caracteres")
    private String colonia;

    @Size(max = 50, message = "La ciudad no puede exceder 50 caracteres")
    private String ciudad;

    @Size(max = 50, message = "El estado no puede exceder 50 caracteres")
    private String estado;

    @Pattern(regexp = "^\\d{5}$", message = "El código postal debe tener 5 dígitos")
    private String codigoPostal;

    private String pais;
}
