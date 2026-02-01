package com.liverpool.customer.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${server.servlet.context-path:/customer-service}")
    private String contextPath;

    @Bean
    public OpenAPI customerServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Service API")
                        .description("API REST para gestión de clientes - Sistema de Pedidos Liverpool")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Liverpool DevTeam")
                                .email("dev@liverpool.com")
                                .url("https://www.liverpool.com"))
                        .license(new License()
                                .name("Uso interno Liverpool")
                                .url("https://www.liverpool.com/terms")));
    }
}
