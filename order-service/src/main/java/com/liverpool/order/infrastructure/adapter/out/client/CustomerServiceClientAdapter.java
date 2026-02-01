package com.liverpool.order.infrastructure.adapter.out.client;

import com.liverpool.order.domain.exception.CustomerNotFoundException;
import com.liverpool.order.domain.exception.CustomerServiceUnavailableException;
import com.liverpool.order.domain.port.out.CustomerValidationPort;
import com.liverpool.order.infrastructure.adapter.out.client.dto.CustomerApiResponse;
import com.liverpool.order.infrastructure.adapter.out.client.dto.CustomerExistsResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerServiceClientAdapter implements CustomerValidationPort {

    private final CustomerServiceClient customerServiceClient;

    @Override
    @CircuitBreaker(name = "customerService", fallbackMethod = "customerExistsFallback")
    @Retry(name = "customerService")
    public boolean customerExists(String customerId) {
        try {
            log.debug("Validando existencia de cliente con ID: {}", customerId);
            CustomerApiResponse<CustomerExistsResponse> apiResponse = customerServiceClient.customerExists(customerId);

            if (apiResponse == null || !apiResponse.isSuccess() || apiResponse.getData() == null) {
                log.warn("Respuesta inválida del customer-service para cliente: {}", customerId);
                throw new CustomerNotFoundException("Cliente no encontrado: " + customerId);
            }

            CustomerExistsResponse data = apiResponse.getData();
            boolean exists = data.isExists();
            log.debug("Cliente {} existe: {}", customerId, exists);
            return exists;
        } catch (FeignException.NotFound e) {
            log.warn("Cliente no encontrado: {}", customerId);
            throw new CustomerNotFoundException("Cliente no encontrado: " + customerId);
        } catch (FeignException e) {
            log.error("Error al comunicarse con Customer Service: {}", e.getMessage());
            throw new CustomerServiceUnavailableException(
                    "Servicio de clientes no disponible", e
            );
        }
    }

    @SuppressWarnings("unused")
    private boolean customerExistsFallback(String customerId, Exception ex) {
        log.error("Circuit breaker activado para customerId: {}. Error: {}",
                customerId, ex.getMessage());
        throw new CustomerServiceUnavailableException(
                "El servicio de clientes no está disponible en este momento. " +
                        "Por favor, intente nuevamente más tarde."
        );
    }
}
