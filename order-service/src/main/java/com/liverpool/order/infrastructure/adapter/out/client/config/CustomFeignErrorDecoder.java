package com.liverpool.order.infrastructure.adapter.out.client.config;

import com.liverpool.order.domain.exception.CustomerNotFoundException;
import com.liverpool.order.domain.exception.CustomerServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Error en llamada Feign. Method: {}, Status: {}", methodKey, response.status());

        return switch (response.status()) {
            case 404 -> {
                log.warn("Cliente no encontrado en Customer Service");
                yield new CustomerNotFoundException("Cliente no encontrado");
            }
            case 503, 502, 504 -> {
                log.error("Customer Service no disponible. Status: {}", response.status());
                yield new CustomerServiceUnavailableException(
                        "El servicio de clientes no está disponible temporalmente"
                );
            }
            default -> {
                log.error("Error inesperado en Customer Service. Status: {}", response.status());
                yield defaultDecoder.decode(methodKey, response);
            }
        };
    }
}
