package com.liverpool.customer.infrastructure.adapter.in.rest.mapper;

import com.liverpool.customer.domain.model.Address;
import com.liverpool.customer.domain.model.Customer;
import com.liverpool.customer.domain.port.in.CreateCustomerUseCase.CreateCustomerCommand;
import com.liverpool.customer.domain.port.in.UpdateCustomerUseCase.PatchAddressCommand;
import com.liverpool.customer.domain.port.in.UpdateCustomerUseCase.PatchCustomerCommand;
import com.liverpool.customer.domain.port.in.UpdateCustomerUseCase.UpdateCustomerCommand;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.AddressRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.CreateCustomerRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.PatchAddressRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.PatchCustomerRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.UpdateCustomerRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.AddressResponse;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.CustomerBasicResponse;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.CustomerDetailResponse;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CustomerRestMapper {

    @Mapping(target = "calle", source = "direccionEnvio.calle")
    @Mapping(target = "numeroExterior", source = "direccionEnvio.numeroExterior")
    @Mapping(target = "numeroInterior", source = "direccionEnvio.numeroInterior")
    @Mapping(target = "colonia", source = "direccionEnvio.colonia")
    @Mapping(target = "ciudad", source = "direccionEnvio.ciudad")
    @Mapping(target = "estado", source = "direccionEnvio.estado")
    @Mapping(target = "codigoPostal", source = "direccionEnvio.codigoPostal")
    @Mapping(target = "pais", source = "direccionEnvio.pais")
    CreateCustomerCommand toCommand(CreateCustomerRequest request);

    @Mapping(target = "calle", source = "direccionEnvio.calle")
    @Mapping(target = "numeroExterior", source = "direccionEnvio.numeroExterior")
    @Mapping(target = "numeroInterior", source = "direccionEnvio.numeroInterior")
    @Mapping(target = "colonia", source = "direccionEnvio.colonia")
    @Mapping(target = "ciudad", source = "direccionEnvio.ciudad")
    @Mapping(target = "estado", source = "direccionEnvio.estado")
    @Mapping(target = "codigoPostal", source = "direccionEnvio.codigoPostal")
    @Mapping(target = "pais", source = "direccionEnvio.pais")
    UpdateCustomerCommand toCommand(UpdateCustomerRequest request);

    PatchCustomerCommand toCommand(PatchCustomerRequest request);

    PatchAddressCommand toCommand(PatchAddressRequest request);

    CustomerResponse toResponse(Customer customer);

    CustomerDetailResponse toDetailResponse(Customer customer);

    AddressResponse toResponse(Address address);

    List<CustomerResponse> toResponseList(List<Customer> customers);

    @Mapping(target = "nombreCompleto", source = "customer", qualifiedByName = "toNombreCompleto")
    @Mapping(target = "direccionEnvio", source = "direccionEnvio")
    CustomerBasicResponse toBasicResponse(Customer customer);

    @Named("toNombreCompleto")
    default String toNombreCompleto(Customer customer) {
        return String.format("%s %s %s",
                customer.getNombre(),
                customer.getApellidoPaterno(),
                customer.getApellidoMaterno());
    }
}
