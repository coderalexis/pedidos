package com.liverpool.customer.domain.port.in;

import com.liverpool.customer.domain.model.Customer;

public interface UpdateCustomerUseCase {
    Customer updateCustomer(String id, UpdateCustomerCommand command);
    Customer patchCustomer(String id, PatchCustomerCommand command);

    record UpdateCustomerCommand(
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

    record PatchCustomerCommand(
            String email,
            PatchAddressCommand direccionEnvio
    ) {}

    record PatchAddressCommand(
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
