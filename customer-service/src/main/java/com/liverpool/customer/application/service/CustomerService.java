package com.liverpool.customer.application.service;

import com.liverpool.customer.domain.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.domain.exception.CustomerNotFoundException;
import com.liverpool.customer.domain.model.Address;
import com.liverpool.customer.domain.model.Customer;
import com.liverpool.customer.domain.port.in.CreateCustomerUseCase;
import com.liverpool.customer.domain.port.in.DeleteCustomerUseCase;
import com.liverpool.customer.domain.port.in.GetCustomerUseCase;
import com.liverpool.customer.domain.port.in.UpdateCustomerUseCase;
import com.liverpool.customer.domain.port.in.UpdateCustomerUseCase.PatchAddressCommand;
import com.liverpool.customer.domain.port.in.ValidateCustomerExistsUseCase;
import com.liverpool.customer.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CustomerService implements
        CreateCustomerUseCase,
        GetCustomerUseCase,
        UpdateCustomerUseCase,
        DeleteCustomerUseCase,
        ValidateCustomerExistsUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    public Customer createCustomer(CreateCustomerCommand command) {
        log.info("Creando nuevo cliente con email: {}", command.email());

        if (customerRepositoryPort.existsByEmail(command.email())) {
            log.warn("Ya existe un cliente con el email: {}", command.email());
            throw new CustomerAlreadyExistsException(command.email());
        }

        Address direccion = Address.builder()
                .calle(command.calle())
                .numeroExterior(command.numeroExterior())
                .numeroInterior(command.numeroInterior())
                .colonia(command.colonia())
                .ciudad(command.ciudad())
                .estado(command.estado())
                .codigoPostal(command.codigoPostal())
                .pais(command.pais() != null ? command.pais() : "México")
                .build();

        String customerId = UUID.randomUUID().toString();

        Customer customer = Customer.builder()
                .customerId(customerId)
                .nombre(command.nombre())
                .apellidoPaterno(command.apellidoPaterno())
                .apellidoMaterno(command.apellidoMaterno())
                .email(command.email())
                .direccionEnvio(direccion)
                .activo(true)
                .build();

        Customer savedCustomer = customerRepositoryPort.save(customer);
        log.info("Cliente creado exitosamente con customerId: {}", savedCustomer.getCustomerId());

        return savedCustomer;
    }

    @Override
    public Customer getCustomerById(String customerId) {
        log.debug("Buscando cliente con customerId: {}", customerId);
        return customerRepositoryPort.findByCustomerId(customerId)
                .filter(Customer::getActivo)
                .orElseThrow(() -> {
                    log.warn("Cliente no encontrado con customerId: {}", customerId);
                    return new CustomerNotFoundException(customerId);
                });
    }

    @Override
    public List<Customer> getAllCustomers(int page, int size) {
        log.debug("Obteniendo lista de clientes - página: {}, tamaño: {}", page, size);
        return customerRepositoryPort.findAll(page, size);
    }

    @Override
    public Optional<Customer> getCustomerByEmail(String email) {
        log.debug("Buscando cliente con email: {}", email);
        return customerRepositoryPort.findByEmail(email)
                .filter(Customer::getActivo);
    }

    @Override
    public long getTotalCustomers() {
        return customerRepositoryPort.count();
    }

    @Override
    public Customer updateCustomer(String customerId, UpdateCustomerCommand command) {
        log.info("Actualizando cliente con customerId: {}", customerId);

        Customer existingCustomer = getCustomerById(customerId);

        if (!existingCustomer.getEmail().equals(command.email()) &&
            customerRepositoryPort.existsByEmail(command.email())) {
            log.warn("El email {} ya está en uso por otro cliente", command.email());
            throw new CustomerAlreadyExistsException(command.email());
        }

        Address nuevaDireccion = Address.builder()
                .calle(command.calle())
                .numeroExterior(command.numeroExterior())
                .numeroInterior(command.numeroInterior())
                .colonia(command.colonia())
                .ciudad(command.ciudad())
                .estado(command.estado())
                .codigoPostal(command.codigoPostal())
                .pais(command.pais() != null ? command.pais() : "México")
                .build();

        Customer updatedCustomer = Customer.builder()
                .customerId(existingCustomer.getCustomerId())
                .nombre(command.nombre())
                .apellidoPaterno(command.apellidoPaterno())
                .apellidoMaterno(command.apellidoMaterno())
                .email(command.email())
                .direccionEnvio(nuevaDireccion)
                .activo(existingCustomer.getActivo())
                .fechaCreacion(existingCustomer.getFechaCreacion())
                .build();

        Customer savedCustomer = customerRepositoryPort.save(updatedCustomer);
        log.info("Cliente actualizado exitosamente con customerId: {}", savedCustomer.getCustomerId());

        return savedCustomer;
    }

    @Override
    public Customer patchCustomer(String customerId, PatchCustomerCommand command) {
        log.info("Actualizando parcialmente cliente con customerId: {}", customerId);

        Customer existingCustomer = getCustomerById(customerId);

        if (command.email() != null &&
            !existingCustomer.getEmail().equals(command.email()) &&
            customerRepositoryPort.existsByEmail(command.email())) {
            log.warn("El email {} ya está en uso por otro cliente", command.email());
            throw new CustomerAlreadyExistsException(command.email());
        }

        Address direccionActualizada = buildPatchedAddress(existingCustomer.getDireccionEnvio(), command.direccionEnvio());

        Customer patchedCustomer = Customer.builder()
                .customerId(existingCustomer.getCustomerId())
                .nombre(existingCustomer.getNombre())
                .apellidoPaterno(existingCustomer.getApellidoPaterno())
                .apellidoMaterno(existingCustomer.getApellidoMaterno())
                .email(command.email() != null ? command.email() : existingCustomer.getEmail())
                .direccionEnvio(direccionActualizada)
                .activo(existingCustomer.getActivo())
                .fechaCreacion(existingCustomer.getFechaCreacion())
                .build();

        Customer savedCustomer = customerRepositoryPort.save(patchedCustomer);
        log.info("Cliente parcialmente actualizado con customerId: {}", savedCustomer.getCustomerId());

        return savedCustomer;
    }

    private Address buildPatchedAddress(Address existingAddress, PatchAddressCommand addressCommand) {
        if (addressCommand == null) {
            return existingAddress;
        }

        return Address.builder()
                .calle(addressCommand.calle() != null ? addressCommand.calle() : existingAddress.getCalle())
                .numeroExterior(addressCommand.numeroExterior() != null ? addressCommand.numeroExterior() : existingAddress.getNumeroExterior())
                .numeroInterior(addressCommand.numeroInterior() != null ? addressCommand.numeroInterior() : existingAddress.getNumeroInterior())
                .colonia(addressCommand.colonia() != null ? addressCommand.colonia() : existingAddress.getColonia())
                .ciudad(addressCommand.ciudad() != null ? addressCommand.ciudad() : existingAddress.getCiudad())
                .estado(addressCommand.estado() != null ? addressCommand.estado() : existingAddress.getEstado())
                .codigoPostal(addressCommand.codigoPostal() != null ? addressCommand.codigoPostal() : existingAddress.getCodigoPostal())
                .pais(addressCommand.pais() != null ? addressCommand.pais() : existingAddress.getPais())
                .build();
    }

    @Override
    public void deleteCustomer(String customerId) {
        log.info("Eliminando (soft delete) cliente con customerId: {}", customerId);

        Customer existingCustomer = getCustomerById(customerId);

        Customer deletedCustomer = Customer.builder()
                .customerId(existingCustomer.getCustomerId())
                .nombre(existingCustomer.getNombre())
                .apellidoPaterno(existingCustomer.getApellidoPaterno())
                .apellidoMaterno(existingCustomer.getApellidoMaterno())
                .email(existingCustomer.getEmail())
                .direccionEnvio(existingCustomer.getDireccionEnvio())
                .activo(false)
                .fechaCreacion(existingCustomer.getFechaCreacion())
                .build();

        customerRepositoryPort.save(deletedCustomer);
        log.info("Cliente desactivado (soft delete) exitosamente con customerId: {}", customerId);
    }

    @Override
    public boolean customerExists(String customerId) {
        log.debug("Verificando existencia de cliente con customerId: {}", customerId);
        return customerRepositoryPort.findByCustomerId(customerId)
                .map(Customer::getActivo)
                .orElse(false);
    }

    @Override
    public Customer getBasicCustomerInfo(String customerId) {
        log.debug("Obteniendo información básica del cliente con customerId: {}", customerId);
        return getCustomerById(customerId);
    }

    @Override
    public Map<String, Boolean> validateMultipleCustomers(List<String> customerIds) {
        log.debug("Validando existencia de {} clientes", customerIds.size());
        Map<String, Boolean> results = new HashMap<>();

        for (String customerId : customerIds) {
            results.put(customerId, customerExists(customerId));
        }

        return results;
    }
}
