package com.liverpool.customer.domain.port.in;

import com.liverpool.customer.domain.model.Customer;

import java.util.List;
import java.util.Optional;

public interface GetCustomerUseCase {
    Customer getCustomerById(String id);
    List<Customer> getAllCustomers(int page, int size);
    Optional<Customer> getCustomerByEmail(String email);
    long getTotalCustomers();
}
