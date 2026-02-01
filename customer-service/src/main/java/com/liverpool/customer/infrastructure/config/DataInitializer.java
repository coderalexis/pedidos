package com.liverpool.customer.infrastructure.config;

import com.liverpool.customer.infrastructure.adapter.out.persistence.document.AddressDocument;
import com.liverpool.customer.infrastructure.adapter.out.persistence.document.CustomerDocument;
import com.liverpool.customer.infrastructure.adapter.out.persistence.repository.CustomerMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    @Profile("dev")
    public CommandLineRunner initData(CustomerMongoRepository customerRepository) {
        return args -> {
            if (customerRepository.count() == 0) {
                log.info("Inicializando datos de prueba en MongoDB...");

                List<CustomerDocument> customers = Arrays.asList(
                        CustomerDocument.builder()
                                .customerId(UUID.randomUUID().toString())
                                .nombre("Juan")
                                .apellidoPaterno("García")
                                .apellidoMaterno("López")
                                .email("juan.garcia@liverpool.com")
                                .direccionEnvio(AddressDocument.builder()
                                        .calle("Av. Insurgentes Sur")
                                        .numeroExterior("1234")
                                        .numeroInterior("PH1")
                                        .colonia("Del Valle")
                                        .ciudad("Ciudad de México")
                                        .estado("CDMX")
                                        .codigoPostal("03100")
                                        .pais("México")
                                        .build())
                                .activo(true)
                                .build(),

                        CustomerDocument.builder()
                                .customerId(UUID.randomUUID().toString())
                                .nombre("María")
                                .apellidoPaterno("Hernández")
                                .apellidoMaterno("Martínez")
                                .email("maria.hernandez@liverpool.com")
                                .direccionEnvio(AddressDocument.builder()
                                        .calle("Paseo de la Reforma")
                                        .numeroExterior("505")
                                        .numeroInterior("301")
                                        .colonia("Cuauhtémoc")
                                        .ciudad("Ciudad de México")
                                        .estado("CDMX")
                                        .codigoPostal("06500")
                                        .pais("México")
                                        .build())
                                .activo(true)
                                .build(),

                        CustomerDocument.builder()
                                .customerId(UUID.randomUUID().toString())
                                .nombre("Carlos")
                                .apellidoPaterno("Rodríguez")
                                .apellidoMaterno("Sánchez")
                                .email("carlos.rodriguez@liverpool.com")
                                .direccionEnvio(AddressDocument.builder()
                                        .calle("Av. Constituyentes")
                                        .numeroExterior("800")
                                        .colonia("Lomas Altas")
                                        .ciudad("Ciudad de México")
                                        .estado("CDMX")
                                        .codigoPostal("11950")
                                        .pais("México")
                                        .build())
                                .activo(true)
                                .build(),

                        CustomerDocument.builder()
                                .customerId(UUID.randomUUID().toString())
                                .nombre("Ana")
                                .apellidoPaterno("Martínez")
                                .apellidoMaterno("González")
                                .email("ana.martinez@liverpool.com")
                                .direccionEnvio(AddressDocument.builder()
                                        .calle("Av. Santa Fe")
                                        .numeroExterior("440")
                                        .numeroInterior("T2-1501")
                                        .colonia("Santa Fe")
                                        .ciudad("Ciudad de México")
                                        .estado("CDMX")
                                        .codigoPostal("05348")
                                        .pais("México")
                                        .build())
                                .activo(true)
                                .build(),

                        CustomerDocument.builder()
                                .customerId(UUID.randomUUID().toString())
                                .nombre("Roberto")
                                .apellidoPaterno("López")
                                .apellidoMaterno("Pérez")
                                .email("roberto.lopez@liverpool.com")
                                .direccionEnvio(AddressDocument.builder()
                                        .calle("Av. Revolución")
                                        .numeroExterior("1521")
                                        .colonia("Guadalupe Inn")
                                        .ciudad("Ciudad de México")
                                        .estado("CDMX")
                                        .codigoPostal("01020")
                                        .pais("México")
                                        .build())
                                .activo(true)
                                .build()
                );

                customerRepository.saveAll(customers);
                log.info("Se insertaron {} clientes de prueba en MongoDB", customers.size());
            } else {
                log.info("La base de datos ya contiene datos, omitiendo inicialización");
            }
        };
    }
}
