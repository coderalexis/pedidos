package com.liverpool.customer.application.service;

import com.liverpool.customer.domain.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.domain.exception.CustomerNotFoundException;
import com.liverpool.customer.domain.model.Address;
import com.liverpool.customer.domain.model.Customer;
import com.liverpool.customer.domain.port.in.CreateCustomerUseCase.CreateCustomerCommand;
import com.liverpool.customer.domain.port.in.UpdateCustomerUseCase.PatchAddressCommand;
import com.liverpool.customer.domain.port.in.UpdateCustomerUseCase.PatchCustomerCommand;
import com.liverpool.customer.domain.port.in.UpdateCustomerUseCase.UpdateCustomerCommand;
import com.liverpool.customer.domain.port.out.CustomerRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private Address testAddress;
    private String testCustomerId;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID().toString();

        testAddress = Address.builder()
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
    }

    @Nested
    @DisplayName("CreateCustomer Tests")
    class CreateCustomerTests {

        @Test
        @DisplayName("Should create customer successfully")
        void whenCreateCustomer_thenReturnCreatedCustomer() {
            // Given
            CreateCustomerCommand command = new CreateCustomerCommand(
                    "Juan", "García", "López", "juan.garcia@email.com",
                    "Av. Insurgentes", "123", "4A", "Del Valle",
                    "Ciudad de México", "CDMX", "03100", "México"
            );

            when(customerRepositoryPort.existsByEmail(command.email())).thenReturn(false);
            when(customerRepositoryPort.save(any(Customer.class))).thenReturn(testCustomer);

            // When
            Customer result = customerService.createCustomer(command);

            // Then
            assertNotNull(result);
            assertNotNull(result.getCustomerId());
            assertEquals("Juan", result.getNombre());
            assertEquals("juan.garcia@email.com", result.getEmail());
            verify(customerRepositoryPort, times(1)).existsByEmail(command.email());
            verify(customerRepositoryPort, times(1)).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void whenCreateCustomerWithExistingEmail_thenThrowException() {
            // Given
            CreateCustomerCommand command = new CreateCustomerCommand(
                    "Juan", "García", "López", "existing@email.com",
                    "Av. Insurgentes", "123", null, "Del Valle",
                    "Ciudad de México", "CDMX", "03100", "México"
            );

            when(customerRepositoryPort.existsByEmail(command.email())).thenReturn(true);

            // When & Then
            assertThrows(CustomerAlreadyExistsException.class,
                    () -> customerService.createCustomer(command));

            verify(customerRepositoryPort, times(1)).existsByEmail(command.email());
            verify(customerRepositoryPort, never()).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("GetCustomer Tests")
    class GetCustomerTests {

        @Test
        @DisplayName("Should return customer when found by customerId")
        void whenGetCustomerById_thenReturnCustomer() {
            // Given
            when(customerRepositoryPort.findByCustomerId(testCustomerId))
                    .thenReturn(Optional.of(testCustomer));

            // When
            Customer result = customerService.getCustomerById(testCustomerId);

            // Then
            assertNotNull(result);
            assertEquals(testCustomerId, result.getCustomerId());
            assertEquals("Juan", result.getNombre());
            verify(customerRepositoryPort, times(1)).findByCustomerId(testCustomerId);
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void whenGetCustomerById_withInvalidId_thenThrowException() {
            // Given
            String invalidCustomerId = UUID.randomUUID().toString();
            when(customerRepositoryPort.findByCustomerId(invalidCustomerId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThrows(CustomerNotFoundException.class,
                    () -> customerService.getCustomerById(invalidCustomerId));
        }

        @Test
        @DisplayName("Should throw exception when customer is inactive")
        void whenGetInactiveCustomerById_thenThrowException() {
            // Given
            String inactiveCustomerId = UUID.randomUUID().toString();
            Customer inactiveCustomer = Customer.builder()
                    .customerId(inactiveCustomerId)
                    .nombre("Pedro")
                    .activo(false)
                    .build();

            when(customerRepositoryPort.findByCustomerId(inactiveCustomerId))
                    .thenReturn(Optional.of(inactiveCustomer));

            // When & Then
            assertThrows(CustomerNotFoundException.class,
                    () -> customerService.getCustomerById(inactiveCustomerId));
        }

        @Test
        @DisplayName("Should return list of customers with pagination")
        void whenGetAllCustomers_thenReturnPaginatedList() {
            // Given
            List<Customer> customers = Arrays.asList(testCustomer,
                    Customer.builder().customerId(UUID.randomUUID().toString()).nombre("María").activo(true).build());

            when(customerRepositoryPort.findAll(0, 10)).thenReturn(customers);

            // When
            List<Customer> result = customerService.getAllCustomers(0, 10);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(customerRepositoryPort, times(1)).findAll(0, 10);
        }

        @Test
        @DisplayName("Should return customer when found by email")
        void whenGetCustomerByEmail_thenReturnCustomer() {
            // Given
            String email = "juan.garcia@email.com";
            when(customerRepositoryPort.findByEmail(email))
                    .thenReturn(Optional.of(testCustomer));

            // When
            Optional<Customer> result = customerService.getCustomerByEmail(email);

            // Then
            assertTrue(result.isPresent());
            assertEquals(email, result.get().getEmail());
        }
    }

    @Nested
    @DisplayName("UpdateCustomer Tests")
    class UpdateCustomerTests {

        @Test
        @DisplayName("Should update customer successfully")
        void whenUpdateCustomer_thenReturnUpdatedCustomer() {
            // Given
            UpdateCustomerCommand command = new UpdateCustomerCommand(
                    "Juan Carlos", "García", "López", "juan.garcia@email.com",
                    "Nueva Calle", "456", null, "Polanco",
                    "Ciudad de México", "CDMX", "11560", "México"
            );

            when(customerRepositoryPort.findByCustomerId(testCustomerId))
                    .thenReturn(Optional.of(testCustomer));
            when(customerRepositoryPort.save(any(Customer.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Customer result = customerService.updateCustomer(testCustomerId, command);

            // Then
            assertNotNull(result);
            assertEquals("Juan Carlos", result.getNombre());
            assertEquals("Nueva Calle", result.getDireccionEnvio().getCalle());
            verify(customerRepositoryPort, times(1)).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw exception when updating with existing email")
        void whenUpdateCustomerWithExistingEmail_thenThrowException() {
            // Given
            UpdateCustomerCommand command = new UpdateCustomerCommand(
                    "Juan", "García", "López", "existing@email.com",
                    "Calle", "123", null, "Colonia",
                    "Ciudad", "Estado", "12345", "México"
            );

            when(customerRepositoryPort.findByCustomerId(testCustomerId))
                    .thenReturn(Optional.of(testCustomer));
            when(customerRepositoryPort.existsByEmail("existing@email.com"))
                    .thenReturn(true);

            // When & Then
            assertThrows(CustomerAlreadyExistsException.class,
                    () -> customerService.updateCustomer(testCustomerId, command));
        }

        @Test
        @DisplayName("Should patch customer email successfully")
        void whenPatchCustomerEmail_thenUpdateOnlyEmail() {
            // Given
            PatchCustomerCommand command = new PatchCustomerCommand(
                    "nuevo.email@email.com",
                    null
            );

            when(customerRepositoryPort.findByCustomerId(testCustomerId))
                    .thenReturn(Optional.of(testCustomer));
            when(customerRepositoryPort.existsByEmail("nuevo.email@email.com"))
                    .thenReturn(false);
            when(customerRepositoryPort.save(any(Customer.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Customer result = customerService.patchCustomer(testCustomerId, command);

            // Then
            assertNotNull(result);
            assertEquals("nuevo.email@email.com", result.getEmail());
            assertEquals("Juan", result.getNombre());
            assertEquals("Av. Insurgentes", result.getDireccionEnvio().getCalle());
        }

        @Test
        @DisplayName("Should patch customer address successfully")
        void whenPatchCustomerAddress_thenUpdateOnlyAddress() {
            // Given
            PatchAddressCommand addressCommand = new PatchAddressCommand(
                    "Nueva Calle",
                    null, null, null, null, null, null, null
            );
            PatchCustomerCommand command = new PatchCustomerCommand(
                    null,
                    addressCommand
            );

            when(customerRepositoryPort.findByCustomerId(testCustomerId))
                    .thenReturn(Optional.of(testCustomer));
            when(customerRepositoryPort.save(any(Customer.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Customer result = customerService.patchCustomer(testCustomerId, command);

            // Then
            assertNotNull(result);
            assertEquals("juan.garcia@email.com", result.getEmail());
            assertEquals("Nueva Calle", result.getDireccionEnvio().getCalle());
            assertEquals("123", result.getDireccionEnvio().getNumeroExterior());
            assertEquals("Del Valle", result.getDireccionEnvio().getColonia());
        }
    }

    @Nested
    @DisplayName("DeleteCustomer Tests")
    class DeleteCustomerTests {

        @Test
        @DisplayName("Should soft delete customer successfully")
        void whenDeleteCustomer_thenSetActivoToFalse() {
            // Given
            ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);

            when(customerRepositoryPort.findByCustomerId(testCustomerId))
                    .thenReturn(Optional.of(testCustomer));
            when(customerRepositoryPort.save(any(Customer.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            customerService.deleteCustomer(testCustomerId);

            // Then
            verify(customerRepositoryPort).save(customerCaptor.capture());
            assertFalse(customerCaptor.getValue().getActivo());
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent customer")
        void whenDeleteNonExistentCustomer_thenThrowException() {
            // Given
            String invalidCustomerId = UUID.randomUUID().toString();
            when(customerRepositoryPort.findByCustomerId(invalidCustomerId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThrows(CustomerNotFoundException.class,
                    () -> customerService.deleteCustomer(invalidCustomerId));
        }
    }

    @Nested
    @DisplayName("ValidateCustomerExists Tests")
    class ValidateCustomerExistsTests {

        @Test
        @DisplayName("Should return true when customer exists and is active")
        void whenCustomerExistsAndActive_thenReturnTrue() {
            // Given
            when(customerRepositoryPort.findByCustomerId(testCustomerId))
                    .thenReturn(Optional.of(testCustomer));

            // When
            boolean result = customerService.customerExists(testCustomerId);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when customer does not exist")
        void whenCustomerDoesNotExist_thenReturnFalse() {
            // Given
            String nonExistentCustomerId = UUID.randomUUID().toString();
            when(customerRepositoryPort.findByCustomerId(nonExistentCustomerId))
                    .thenReturn(Optional.empty());

            // When
            boolean result = customerService.customerExists(nonExistentCustomerId);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should validate multiple customers correctly")
        void whenValidateMultipleCustomers_thenReturnCorrectResults() {
            // Given
            String customerId1 = testCustomerId;
            String customerId2 = UUID.randomUUID().toString();
            String customerId999 = UUID.randomUUID().toString();
            List<String> customerIds = Arrays.asList(customerId1, customerId2, customerId999);

            Customer customer2 = Customer.builder()
                    .customerId(customerId2)
                    .activo(true)
                    .build();

            when(customerRepositoryPort.findByCustomerId(customerId1))
                    .thenReturn(Optional.of(testCustomer));
            when(customerRepositoryPort.findByCustomerId(customerId2))
                    .thenReturn(Optional.of(customer2));
            when(customerRepositoryPort.findByCustomerId(customerId999))
                    .thenReturn(Optional.empty());

            // When
            Map<String, Boolean> results = customerService.validateMultipleCustomers(customerIds);

            // Then
            assertEquals(3, results.size());
            assertTrue(results.get(customerId1));
            assertTrue(results.get(customerId2));
            assertFalse(results.get(customerId999));
        }
    }
}
