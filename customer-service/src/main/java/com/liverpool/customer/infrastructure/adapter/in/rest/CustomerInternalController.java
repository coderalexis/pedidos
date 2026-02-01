package com.liverpool.customer.infrastructure.adapter.in.rest;

import com.liverpool.customer.domain.model.Customer;
import com.liverpool.customer.domain.port.in.ValidateCustomerExistsUseCase;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.BatchCustomerExistsRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.BatchCustomerExistsResponse;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.CustomerBasicResponse;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.response.CustomerExistsResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/internal/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Internal Customers", description = "API interna para validación de clientes (uso exclusivo de Order Service)")
public class CustomerInternalController {

    private final ValidateCustomerExistsUseCase validateCustomerExistsUseCase;
    private final CustomerRestMapper customerRestMapper;

    @GetMapping("/{id}/exists")
    @Operation(summary = "Validar existencia de cliente",
            description = "Verifica si un cliente existe y está activo en el sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validación completada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<CustomerExistsResponse>> customerExists(
            @Parameter(description = "ID del cliente a validar")
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        log.debug("Validación interna de existencia del cliente con ID: {}", id);

        boolean exists = validateCustomerExistsUseCase.customerExists(id);

        CustomerExistsResponse data = CustomerExistsResponse.builder()
                .customerId(id)
                .exists(exists)
                .build();

        String message = exists ? "Cliente existe y está activo" : "Cliente no encontrado o inactivo";

        return ResponseEntity.ok(ApiResponse.success(data, message, httpRequest.getRequestURI()));
    }

    @GetMapping("/{id}/basic")
    @Operation(summary = "Obtener información básica del cliente",
            description = "Obtiene información resumida del cliente para uso interno")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Información obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ApiResponse<CustomerBasicResponse>> getBasicCustomerInfo(
            @Parameter(description = "ID del cliente")
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        log.debug("Solicitud interna de información básica del cliente con ID: {}", id);

        Customer customer = validateCustomerExistsUseCase.getBasicCustomerInfo(id);
        CustomerBasicResponse data = customerRestMapper.toBasicResponse(customer);

        return ResponseEntity.ok(ApiResponse.success(data, "Información del cliente obtenida", httpRequest.getRequestURI()));
    }

    @PostMapping("/batch/exists")
    @Operation(summary = "Validar múltiples clientes",
            description = "Verifica la existencia de múltiples clientes en una sola petición")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validación completada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<ApiResponse<BatchCustomerExistsResponse>> validateMultipleCustomers(
            @Valid @RequestBody BatchCustomerExistsRequest request,
            HttpServletRequest httpRequest) {
        log.debug("Validación batch de {} clientes", request.getCustomerIds().size());

        Map<String, Boolean> results = validateCustomerExistsUseCase
                .validateMultipleCustomers(request.getCustomerIds());

        int totalExisting = (int) results.values().stream()
                .filter(exists -> exists)
                .count();

        BatchCustomerExistsResponse data = BatchCustomerExistsResponse.builder()
                .results(results)
                .totalRequested(request.getCustomerIds().size())
                .totalExisting(totalExisting)
                .build();

        return ResponseEntity.ok(ApiResponse.success(data, "Validación batch completada", httpRequest.getRequestURI()));
    }
}
