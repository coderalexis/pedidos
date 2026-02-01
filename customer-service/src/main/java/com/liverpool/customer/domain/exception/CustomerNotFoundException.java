package com.liverpool.customer.domain.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String id) {
        super(String.format("Cliente con ID %s no encontrado", id));
    }

    public static CustomerNotFoundException byEmail(String email) {
        return new CustomerNotFoundException(String.format("Cliente con email %s no encontrado", email));
    }
}
