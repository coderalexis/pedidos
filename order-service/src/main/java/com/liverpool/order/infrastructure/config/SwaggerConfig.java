package com.liverpool.order.infrastructure.config;

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

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("API REST para la gestión de pedidos del sistema Liverpool. " +
                                "Este microservicio permite crear, consultar, actualizar y cancelar pedidos.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo Liverpool")
                                .email("dev@liverpool.com.mx")
                                .url("https://www.liverpool.com.mx"))
                        .license(new License()
                                .name("Propietario")
                                .url("https://www.liverpool.com.mx/terminos")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + "/order-service")
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("http://order-service:8082/order-service")
                                .description("Servidor Docker")
                ));
    }
}
