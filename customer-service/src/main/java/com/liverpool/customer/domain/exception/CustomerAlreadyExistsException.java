package com.liverpool.customer.domain.exception;

public class CustomerAlreadyExistsException extends RuntimeException {

    public CustomerAlreadyExistsException(String email) {
        super(String.format("Ya existe un cliente con el email: %s", email));
    }
}
