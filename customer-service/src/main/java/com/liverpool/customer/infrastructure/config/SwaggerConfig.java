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

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI customerServiceOpenAPI() {
        String basePath = (contextPath == null || contextPath.isBlank()) ? "/" : contextPath;

        return new OpenAPI()
                .servers(List.of(new Server().url(basePath)))
                .info(new Info()
                        .title("Customer Service API")
                        .version("1.0.0"));
    }
}
