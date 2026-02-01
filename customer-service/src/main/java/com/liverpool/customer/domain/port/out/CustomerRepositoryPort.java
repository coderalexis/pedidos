package com.liverpool.customer.domain.port.out;

import com.liverpool.customer.domain.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepositoryPort {
    Customer save(Customer customer);
    Optional<Customer> findByCustomerId(String customerId);
    List<Customer> findAll(int page, int size);
    Optional<Customer> findByEmail(String email);
    void deleteByCustomerId(String customerId);
    boolean existsByCustomerId(String customerId);
    boolean existsByEmail(String email);
    long count();
}
