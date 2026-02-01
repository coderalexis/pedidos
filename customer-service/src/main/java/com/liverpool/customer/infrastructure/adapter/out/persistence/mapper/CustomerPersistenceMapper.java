package com.liverpool.customer.infrastructure.adapter.out.persistence.mapper;

import com.liverpool.customer.domain.model.Address;
import com.liverpool.customer.domain.model.Customer;
import com.liverpool.customer.infrastructure.adapter.out.persistence.document.AddressDocument;
import com.liverpool.customer.infrastructure.adapter.out.persistence.document.CustomerDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CustomerPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "direccionEnvio", source = "direccionEnvio")
    CustomerDocument toDocument(Customer customer);

    AddressDocument toDocument(Address address);

    @Mapping(target = "direccionEnvio", source = "direccionEnvio")
    Customer toDomain(CustomerDocument document);

    Address toDomain(AddressDocument document);

    List<Customer> toDomainList(List<CustomerDocument> documents);
}
