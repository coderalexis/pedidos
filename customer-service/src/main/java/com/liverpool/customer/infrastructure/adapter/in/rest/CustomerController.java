package com.liverpool.customer.infrastructure.adapter.in.rest;

import com.liverpool.customer.domain.model.Customer;
import com.liverpool.customer.domain.port.in.CreateCustomerUseCase;
import com.liverpool.customer.domain.port.in.DeleteCustomerUseCase;
import com.liverpool.customer.domain.port.in.GetCustomerUseCase;
import com.liverpool.customer.domain.port.in.UpdateCustomerUseCase;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.CreateCustomerRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.PatchCustomerRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.UpdateCustomerRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.CustomerDetailResponse;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.CustomerResponse;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.PagedResponse;
import com.liverpool.customer.infrastructure.adapter.in.rest.mapper.CustomerRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "API para gestión de clientes")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;
    private final CustomerRestMapper customerRestMapper;

    @PostMapping
    @Operation(summary = "Crear nuevo cliente", description = "Crea un nuevo cliente en el sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cliente creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ya existe un cliente con el mismo email")
    })
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            HttpServletRequest httpRequest) {
        log.info("Solicitud para crear nuevo cliente con email: {}", request.getEmail());

        Customer customer = createCustomerUseCase.createCustomer(
                customerRestMapper.toCommand(request));

        CustomerDetailResponse data = customerRestMapper.toDetailResponse(customer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Cliente creado exitosamente", httpRequest.getRequestURI()));
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Obtiene la lista paginada de clientes activos")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    })
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getAllCustomers(
            @Parameter(description = "Número de página (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        log.info("Solicitud para listar clientes - página: {}, tamaño: {}", page, size);

        List<Customer> customers = getCustomerUseCase.getAllCustomers(page, size);
        long totalElements = getCustomerUseCase.getTotalCustomers();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        PagedResponse<CustomerResponse> pagedData = PagedResponse.<CustomerResponse>builder()
                .content(customerRestMapper.toResponseList(customers))
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();

        return ResponseEntity.ok(ApiResponse.success(pagedData, "Clientes obtenidos exitosamente", httpRequest.getRequestURI()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Obtiene los detalles completos de un cliente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> getCustomerById(
            @Parameter(description = "ID del cliente")
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        log.info("Solicitud para obtener cliente con ID: {}", id);

        Customer customer = getCustomerUseCase.getCustomerById(id);
        CustomerDetailResponse data = customerRestMapper.toDetailResponse(customer);

        return ResponseEntity.ok(ApiResponse.success(data, "Cliente obtenido exitosamente", httpRequest.getRequestURI()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza todos los datos de un cliente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email ya en uso por otro cliente")
    })
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> updateCustomer(
            @Parameter(description = "ID del cliente")
            @PathVariable String id,
            @Valid @RequestBody UpdateCustomerRequest request,
            HttpServletRequest httpRequest) {
        log.info("Solicitud para actualizar cliente con ID: {}", id);

        Customer customer = updateCustomerUseCase.updateCustomer(id,
                customerRestMapper.toCommand(request));

        CustomerDetailResponse data = customerRestMapper.toDetailResponse(customer);

        return ResponseEntity.ok(ApiResponse.success(data, "Cliente actualizado exitosamente", httpRequest.getRequestURI()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente cliente",
            description = "Actualiza solo email o dirección de un cliente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email ya en uso por otro cliente")
    })
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> patchCustomer(
            @Parameter(description = "ID del cliente")
            @PathVariable String id,
            @Valid @RequestBody PatchCustomerRequest request,
            HttpServletRequest httpRequest) {
        log.info("Solicitud para actualizar parcialmente cliente con ID: {}", id);

        Customer customer = updateCustomerUseCase.patchCustomer(id,
                customerRestMapper.toCommand(request));

        CustomerDetailResponse data = customerRestMapper.toDetailResponse(customer);

        return ResponseEntity.ok(ApiResponse.success(data, "Cliente actualizado parcialmente", httpRequest.getRequestURI()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente (soft delete)",
            description = "Desactiva un cliente en el sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente eliminado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @Parameter(description = "ID del cliente")
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        log.info("Solicitud para eliminar cliente con ID: {}", id);

        deleteCustomerUseCase.deleteCustomer(id);

        return ResponseEntity.ok(ApiResponse.success("Cliente eliminado exitosamente", httpRequest.getRequestURI()));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar cliente por email", description = "Busca un cliente por su dirección de email")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> getCustomerByEmail(
            @Parameter(description = "Email del cliente")
            @PathVariable String email,
            HttpServletRequest httpRequest) {
        log.info("Solicitud para buscar cliente con email: {}", email);

        return getCustomerUseCase.getCustomerByEmail(email)
                .map(customer -> {
                    CustomerDetailResponse data = customerRestMapper.toDetailResponse(customer);
                    return ResponseEntity.ok(ApiResponse.success(data, "Cliente encontrado", httpRequest.getRequestURI()));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.success(null, "Cliente no encontrado", httpRequest.getRequestURI())));
    }
}
