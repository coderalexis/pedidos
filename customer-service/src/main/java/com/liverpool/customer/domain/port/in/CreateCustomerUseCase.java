package com.liverpool.customer.domain.port.in;

import com.liverpool.customer.domain.model.Customer;

public interface CreateCustomerUseCase {
    Customer createCustomer(CreateCustomerCommand command);

    record CreateCustomerCommand(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            String email,
            String calle,
            String numeroExterior,
            String numeroInterior,
            String colonia,
            String ciudad,
            String estado,
            String codigoPostal,
            String pais
    ) {}
}
