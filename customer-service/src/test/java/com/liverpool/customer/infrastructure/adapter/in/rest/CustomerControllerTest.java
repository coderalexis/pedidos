package com.liverpool.customer.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liverpool.customer.domain.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.domain.exception.CustomerNotFoundException;
import com.liverpool.customer.domain.model.Address;
import com.liverpool.customer.domain.model.Customer;
import com.liverpool.customer.domain.port.in.CreateCustomerUseCase;
import com.liverpool.customer.domain.port.in.DeleteCustomerUseCase;
import com.liverpool.customer.domain.port.in.GetCustomerUseCase;
import com.liverpool.customer.domain.port.in.UpdateCustomerUseCase;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.AddressRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.CreateCustomerRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.PatchAddressRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.PatchCustomerRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.dto.request.UpdateCustomerRequest;
import com.liverpool.customer.infrastructure.adapter.in.rest.mapper.CustomerRestMapper;
import com.liverpool.customer.infrastructure.adapter.in.rest.mapper.CustomerRestMapperImpl;
import com.liverpool.customer.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController Tests")
class CustomerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CustomerRestMapper customerRestMapper;

    @Mock
    private CreateCustomerUseCase createCustomerUseCase;
    @Mock
    private GetCustomerUseCase getCustomerUseCase;
    @Mock
    private UpdateCustomerUseCase updateCustomerUseCase;
    @Mock
    private DeleteCustomerUseCase deleteCustomerUseCase;

    private Customer testCustomer;
    private CreateCustomerRequest validCreateRequest;
    private String testCustomerId;

    @BeforeEach
    void setUp() {
        customerRestMapper = new CustomerRestMapperImpl();

        CustomerController controller = new CustomerController(
                createCustomerUseCase,
                getCustomerUseCase,
                updateCustomerUseCase,
                deleteCustomerUseCase,
                customerRestMapper
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testCustomerId = UUID.randomUUID().toString();

        Address testAddress = Address.builder()
                .calle("Av. Insurgentes")
                .numeroExterior("123")
                .numeroInterior("4A")
                .colonia("Del Valle")
                .ciudad("Ciudad de México")
                .estado("CDMX")
                .codigoPostal("03100")
                .pais("México")
                .build();

        testCustomer = Customer.builder()
                .customerId(testCustomerId)
                .nombre("Juan")
                .apellidoPaterno("García")
                .apellidoMaterno("López")
                .email("juan.garcia@email.com")
                .direccionEnvio(testAddress)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        validCreateRequest = CreateCustomerRequest.builder()
                .nombre("Juan")
                .apellidoPaterno("García")
                .apellidoMaterno("López")
                .email("juan.garcia@email.com")
                .direccionEnvio(AddressRequest.builder()
                        .calle("Av. Insurgentes")
                        .numeroExterior("123")
                        .numeroInterior("4A")
                        .colonia("Del Valle")
                        .ciudad("Ciudad de México")
                        .estado("CDMX")
                        .codigoPostal("03100")
                        .pais("México")
                        .build())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/customers")
    class CreateCustomerEndpoint {

        @Test
        @DisplayName("Should create customer and return 201 with standard response")
        void whenCreateValidCustomer_thenReturn201() throws Exception {
            when(createCustomerUseCase.createCustomer(any()))
                    .thenReturn(testCustomer);

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Cliente creado exitosamente")))
                    .andExpect(jsonPath("$.data.customerId", is(testCustomerId)))
                    .andExpect(jsonPath("$.data.nombre", is("Juan")))
                    .andExpect(jsonPath("$.data.email", is("juan.garcia@email.com")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()))
                    .andExpect(jsonPath("$.path", is("/api/v1/customers")));

            verify(createCustomerUseCase, times(1)).createCustomer(any());
        }

        @Test
        @DisplayName("Should return 400 with validation error response when request is invalid")
        void whenCreateInvalidCustomer_thenReturn400() throws Exception {
            CreateCustomerRequest invalidRequest = CreateCustomerRequest.builder()
                    .nombre("")  // Invalid: empty
                    .apellidoPaterno("García")
                    .apellidoMaterno("López")
                    .email("invalid-email")  // Invalid: not email format
                    .build();

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
                    .andExpect(jsonPath("$.error.fields", notNullValue()));

            verify(createCustomerUseCase, never()).createCustomer(any());
        }

        @Test
        @DisplayName("Should return 409 with error response when email already exists")
        void whenCreateCustomerWithExistingEmail_thenReturn409() throws Exception {
            when(createCustomerUseCase.createCustomer(any()))
                    .thenThrow(new CustomerAlreadyExistsException("juan.garcia@email.com"));

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error.code", is("CUSTOMER_ALREADY_EXISTS")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers")
    class GetCustomersEndpoint {

        @Test
        @DisplayName("Should return list of customers with pagination in standard response")
        void whenGetAllCustomers_thenReturnPagedList() throws Exception {
            List<Customer> customers = Arrays.asList(testCustomer);
            when(getCustomerUseCase.getAllCustomers(0, 10)).thenReturn(customers);
            when(getCustomerUseCase.getTotalCustomers()).thenReturn(1L);

            mockMvc.perform(get("/api/v1/customers")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Clientes obtenidos exitosamente")))
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.totalElements", is(1)))
                    .andExpect(jsonPath("$.data.page", is(0)))
                    .andExpect(jsonPath("$.data.size", is(10)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/{customerId}")
    class GetCustomerByIdEndpoint {

        @Test
        @DisplayName("Should return customer in standard response when found")
        void whenGetExistingCustomer_thenReturnCustomer() throws Exception {
            when(getCustomerUseCase.getCustomerById(testCustomerId)).thenReturn(testCustomer);

            mockMvc.perform(get("/api/v1/customers/{customerId}", testCustomerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Cliente obtenido exitosamente")))
                    .andExpect(jsonPath("$.data.customerId", is(testCustomerId)))
                    .andExpect(jsonPath("$.data.nombre", is("Juan")))
                    .andExpect(jsonPath("$.data.direccionEnvio.calle", is("Av. Insurgentes")));
        }

        @Test
        @DisplayName("Should return 404 with error response when customer not found")
        void whenGetNonExistingCustomer_thenReturn404() throws Exception {
            String invalidCustomerId = UUID.randomUUID().toString();
            when(getCustomerUseCase.getCustomerById(invalidCustomerId))
                    .thenThrow(new CustomerNotFoundException(invalidCustomerId));

            mockMvc.perform(get("/api/v1/customers/{customerId}", invalidCustomerId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error.code", is("CUSTOMER_NOT_FOUND")));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/customers/{customerId}")
    class UpdateCustomerEndpoint {

        @Test
        @DisplayName("Should update customer and return 200 with standard response")
        void whenUpdateCustomer_thenReturnUpdatedCustomer() throws Exception {
            UpdateCustomerRequest updateRequest = UpdateCustomerRequest.builder()
                    .nombre("Juan Carlos")
                    .apellidoPaterno("García")
                    .apellidoMaterno("López")
                    .email("juan.garcia@email.com")
                    .direccionEnvio(AddressRequest.builder()
                            .calle("Nueva Calle")
                            .numeroExterior("456")
                            .colonia("Polanco")
                            .ciudad("Ciudad de México")
                            .estado("CDMX")
                            .codigoPostal("11560")
                            .pais("México")
                            .build())
                    .build();

            Customer updatedCustomer = Customer.builder()
                    .customerId(testCustomerId)
                    .nombre("Juan Carlos")
                    .apellidoPaterno("García")
                    .apellidoMaterno("López")
                    .email("juan.garcia@email.com")
                    .direccionEnvio(Address.builder()
                            .calle("Nueva Calle")
                            .numeroExterior("456")
                            .colonia("Polanco")
                            .ciudad("Ciudad de México")
                            .estado("CDMX")
                            .codigoPostal("11560")
                            .pais("México")
                            .build())
                    .activo(true)
                    .build();

            when(updateCustomerUseCase.updateCustomer(anyString(), any()))
                    .thenReturn(updatedCustomer);

            mockMvc.perform(put("/api/v1/customers/{customerId}", testCustomerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Cliente actualizado exitosamente")))
                    .andExpect(jsonPath("$.data.nombre", is("Juan Carlos")))
                    .andExpect(jsonPath("$.data.direccionEnvio.calle", is("Nueva Calle")));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/customers/{customerId}")
    class PatchCustomerEndpoint {

        @Test
        @DisplayName("Should patch customer email and return 200 with standard response")
        void whenPatchCustomerEmail_thenReturnPatchedCustomer() throws Exception {
            PatchCustomerRequest patchRequest = PatchCustomerRequest.builder()
                    .email("nuevo.email@email.com")
                    .build();

            Customer patchedCustomer = Customer.builder()
                    .customerId(testCustomerId)
                    .nombre("Juan")
                    .apellidoPaterno("García")
                    .apellidoMaterno("López")
                    .email("nuevo.email@email.com")
                    .direccionEnvio(testCustomer.getDireccionEnvio())
                    .activo(true)
                    .build();

            when(updateCustomerUseCase.patchCustomer(anyString(), any()))
                    .thenReturn(patchedCustomer);

            mockMvc.perform(patch("/api/v1/customers/{customerId}", testCustomerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Cliente actualizado parcialmente")))
                    .andExpect(jsonPath("$.data.email", is("nuevo.email@email.com")))
                    .andExpect(jsonPath("$.data.nombre", is("Juan")));
        }

        @Test
        @DisplayName("Should patch customer address and return 200 with standard response")
        void whenPatchCustomerAddress_thenReturnPatchedCustomer() throws Exception {
            PatchAddressRequest addressRequest = PatchAddressRequest.builder()
                    .calle("Nueva Calle")
                    .numeroExterior("999")
                    .build();

            PatchCustomerRequest patchRequest = PatchCustomerRequest.builder()
                    .direccionEnvio(addressRequest)
                    .build();

            Customer patchedCustomer = Customer.builder()
                    .customerId(testCustomerId)
                    .nombre("Juan")
                    .apellidoPaterno("García")
                    .apellidoMaterno("López")
                    .email("juan.garcia@email.com")
                    .direccionEnvio(Address.builder()
                            .calle("Nueva Calle")
                            .numeroExterior("999")
                            .colonia("Del Valle")
                            .ciudad("Ciudad de México")
                            .estado("CDMX")
                            .codigoPostal("03100")
                            .pais("México")
                            .build())
                    .activo(true)
                    .build();

            when(updateCustomerUseCase.patchCustomer(anyString(), any()))
                    .thenReturn(patchedCustomer);

            mockMvc.perform(patch("/api/v1/customers/{customerId}", testCustomerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.direccionEnvio.calle", is("Nueva Calle")))
                    .andExpect(jsonPath("$.data.direccionEnvio.numeroExterior", is("999")));
        }

        @Test
        @DisplayName("Should return 404 with error response when patching non-existent customer")
        void whenPatchNonExistingCustomer_thenReturn404() throws Exception {
            String invalidCustomerId = UUID.randomUUID().toString();

            PatchCustomerRequest patchRequest = PatchCustomerRequest.builder()
                    .email("nuevo.email@email.com")
                    .build();

            when(updateCustomerUseCase.patchCustomer(anyString(), any()))
                    .thenThrow(new CustomerNotFoundException(invalidCustomerId));

            mockMvc.perform(patch("/api/v1/customers/{customerId}", invalidCustomerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error.code", is("CUSTOMER_NOT_FOUND")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/customers/{customerId}")
    class DeleteCustomerEndpoint {

        @Test
        @DisplayName("Should delete customer and return 200 with success message")
        void whenDeleteCustomer_thenReturn200() throws Exception {
            doNothing().when(deleteCustomerUseCase).deleteCustomer(testCustomerId);

            mockMvc.perform(delete("/api/v1/customers/{customerId}", testCustomerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Cliente eliminado exitosamente")));

            verify(deleteCustomerUseCase, times(1)).deleteCustomer(testCustomerId);
        }

        @Test
        @DisplayName("Should return 404 with error response when deleting non-existent customer")
        void whenDeleteNonExistingCustomer_thenReturn404() throws Exception {
            String invalidCustomerId = UUID.randomUUID().toString();
            doThrow(new CustomerNotFoundException(invalidCustomerId))
                    .when(deleteCustomerUseCase).deleteCustomer(invalidCustomerId);

            mockMvc.perform(delete("/api/v1/customers/{customerId}", invalidCustomerId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error.code", is("CUSTOMER_NOT_FOUND")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/email/{email}")
    class GetCustomerByEmailEndpoint {

        @Test
        @DisplayName("Should return customer in standard response when found by email")
        void whenGetCustomerByEmail_thenReturnCustomer() throws Exception {
            when(getCustomerUseCase.getCustomerByEmail("juan.garcia@email.com"))
                    .thenReturn(Optional.of(testCustomer));

            mockMvc.perform(get("/api/v1/customers/email/{email}", "juan.garcia@email.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Cliente encontrado")))
                    .andExpect(jsonPath("$.data.email", is("juan.garcia@email.com")));
        }

        @Test
        @DisplayName("Should return 404 when customer not found by email")
        void whenGetCustomerByNonExistingEmail_thenReturn404() throws Exception {
            when(getCustomerUseCase.getCustomerByEmail("unknown@email.com"))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/customers/email/{email}", "unknown@email.com"))
                    .andExpect(status().isNotFound());
        }
    }
}
