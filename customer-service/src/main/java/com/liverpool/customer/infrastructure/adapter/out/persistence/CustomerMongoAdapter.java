package com.liverpool.customer.infrastructure.adapter.out.persistence;

import com.liverpool.customer.domain.model.Customer;
import com.liverpool.customer.domain.port.out.CustomerRepositoryPort;
import com.liverpool.customer.infrastructure.adapter.out.persistence.document.CustomerDocument;
import com.liverpool.customer.infrastructure.adapter.out.persistence.mapper.CustomerPersistenceMapper;
import com.liverpool.customer.infrastructure.adapter.out.persistence.repository.CustomerMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerMongoAdapter implements CustomerRepositoryPort {

    private final CustomerMongoRepository customerMongoRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;

    @Override
    public Customer save(Customer customer) {
        log.debug("Guardando cliente en MongoDB");
        CustomerDocument document = customerPersistenceMapper.toDocument(customer);

        if (customer.getCustomerId() != null) {
            customerMongoRepository.findByCustomerId(customer.getCustomerId())
                    .ifPresent(existingDoc -> document.setId(existingDoc.getId()));
        }

        CustomerDocument savedDocument = customerMongoRepository.save(document);
        return customerPersistenceMapper.toDomain(savedDocument);
    }

    @Override
    public Optional<Customer> findByCustomerId(String customerId) {
        log.debug("Buscando cliente por customerId: {}", customerId);
        return customerMongoRepository.findByCustomerId(customerId)
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public List<Customer> findAll(int page, int size) {
        log.debug("Obteniendo todos los clientes activos - página: {}, tamaño: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        List<CustomerDocument> documents = customerMongoRepository.findAllActive(pageRequest).getContent();
        return customerPersistenceMapper.toDomainList(documents);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        log.debug("Buscando cliente por email: {}", email);
        return customerMongoRepository.findByEmail(email)
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public void deleteByCustomerId(String customerId) {
        log.debug("Eliminando cliente con customerId: {}", customerId);
        customerMongoRepository.deleteByCustomerId(customerId);
    }

    @Override
    public boolean existsByCustomerId(String customerId) {
        return customerMongoRepository.existsByCustomerId(customerId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerMongoRepository.existsByEmail(email);
    }

    @Override
    public long count() {
        return customerMongoRepository.countActive();
    }
}
