package com.liverpool.customer.infrastructure.adapter.out.persistence.repository;

import com.liverpool.customer.infrastructure.adapter.out.persistence.document.CustomerDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerMongoRepository extends MongoRepository<CustomerDocument, String> {

    Optional<CustomerDocument> findByCustomerId(String customerId);

    Optional<CustomerDocument> findByEmail(String email);

    boolean existsByCustomerId(String customerId);

    boolean existsByEmail(String email);

    @Query("{ 'activo': true }")
    Page<CustomerDocument> findAllActive(Pageable pageable);

    @Query(value = "{ 'activo': true }", count = true)
    long countActive();

    Optional<CustomerDocument> findByCustomerIdAndActivoTrue(String customerId);

    void deleteByCustomerId(String customerId);
}
