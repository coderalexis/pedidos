package com.liverpool.customer.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCustomerExistsRequest {

    @NotEmpty(message = "La lista de IDs no puede estar vacía")
    @Size(max = 100, message = "No se pueden validar más de 100 clientes a la vez")
    private List<String> customerIds;
}
