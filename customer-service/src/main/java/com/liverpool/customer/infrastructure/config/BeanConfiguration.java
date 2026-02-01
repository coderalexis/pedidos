package com.liverpool.customer.infrastructure.config;

import com.liverpool.customer.application.service.CustomerService;
import com.liverpool.customer.domain.port.in.*;
import com.liverpool.customer.domain.port.out.CustomerRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CustomerService customerService(CustomerRepositoryPort customerRepositoryPort) {
        return new CustomerService(customerRepositoryPort);
    }

    @Bean
    public CreateCustomerUseCase createCustomerUseCase(CustomerService customerService) {
        return customerService;
    }

    @Bean
    public GetCustomerUseCase getCustomerUseCase(CustomerService customerService) {
        return customerService;
    }

    @Bean
    public UpdateCustomerUseCase updateCustomerUseCase(CustomerService customerService) {
        return customerService;
    }

    @Bean
    public DeleteCustomerUseCase deleteCustomerUseCase(CustomerService customerService) {
        return customerService;
    }

    @Bean
    public ValidateCustomerExistsUseCase validateCustomerExistsUseCase(CustomerService customerService) {
        return customerService;
    }
}
