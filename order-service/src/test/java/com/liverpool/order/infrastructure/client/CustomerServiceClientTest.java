package com.liverpool.order.infrastructure.client;

import com.liverpool.order.domain.exception.CustomerNotFoundException;
import com.liverpool.order.infrastructure.adapter.out.client.CustomerServiceClient;
import com.liverpool.order.infrastructure.adapter.out.client.CustomerServiceClientAdapter;
import com.liverpool.order.infrastructure.adapter.out.client.dto.CustomerApiResponse;
import com.liverpool.order.infrastructure.adapter.out.client.dto.CustomerExistsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerServiceClient Tests")
class CustomerServiceClientTest {

    @Mock
    private CustomerServiceClient customerServiceClient;

    private CustomerServiceClientAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerServiceClientAdapter(customerServiceClient);
    }

    @Nested
    @DisplayName("Customer Exists Tests")
    class CustomerExistsTests {

        @Test
        @DisplayName("Should return true when customer exists")
        void shouldReturnTrueWhenCustomerExists() {
            // Given
            String customerId = "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f";
            CustomerExistsResponse data = CustomerExistsResponse.builder()
                    .customerId(customerId)
                    .exists(true)
                    .message("Cliente encontrado")
                    .build();
            CustomerApiResponse<CustomerExistsResponse> apiResponse = CustomerApiResponse.<CustomerExistsResponse>builder()
                    .success(true)
                    .data(data)
                    .message("Cliente existe y está activo")
                    .build();
            when(customerServiceClient.customerExists(customerId)).thenReturn(apiResponse);

            // When
            boolean result = adapter.customerExists(customerId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when customer does not exist")
        void shouldReturnFalseWhenCustomerDoesNotExist() {
            // Given
            String customerId = "99999999-9999-9999-9999-999999999999";
            CustomerExistsResponse data = CustomerExistsResponse.builder()
                    .customerId(customerId)
                    .exists(false)
                    .message("Cliente no encontrado")
                    .build();
            CustomerApiResponse<CustomerExistsResponse> apiResponse = CustomerApiResponse.<CustomerExistsResponse>builder()
                    .success(true)
                    .data(data)
                    .message("Cliente no encontrado o inactivo")
                    .build();
            when(customerServiceClient.customerExists(customerId)).thenReturn(apiResponse);

            // When
            boolean result = adapter.customerExists(customerId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should throw CustomerNotFoundException when 404 received")
        void shouldThrowCustomerNotFoundExceptionWhen404() {
            // Given
            String customerId = "99999999-9999-9999-9999-999999999999";
            when(customerServiceClient.customerExists(customerId))
                    .thenThrow(new feign.FeignException.NotFound(
                            "Not Found",
                            feign.Request.create(
                                    feign.Request.HttpMethod.GET,
                                    "/internal/api/v1/customers/" + customerId + "/exists",
                                    java.util.Collections.emptyMap(),
                                    null,
                                    null,
                                    null
                            ),
                            null,
                            null
                    ));

            // When/Then
            assertThatThrownBy(() -> adapter.customerExists(customerId))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining("Cliente no encontrado");
        }

        @Test
        @DisplayName("Should throw CustomerNotFoundException when response is null")
        void shouldThrowCustomerNotFoundExceptionWhenResponseIsNull() {
            // Given
            String customerId = "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f";
            when(customerServiceClient.customerExists(customerId)).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> adapter.customerExists(customerId))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining("Cliente no encontrado");
        }

        @Test
        @DisplayName("Should throw CustomerNotFoundException when response is not successful")
        void shouldThrowCustomerNotFoundExceptionWhenResponseNotSuccessful() {
            // Given
            String customerId = "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f";
            CustomerApiResponse<CustomerExistsResponse> apiResponse = CustomerApiResponse.<CustomerExistsResponse>builder()
                    .success(false)
                    .data(null)
                    .message("Error")
                    .build();
            when(customerServiceClient.customerExists(customerId)).thenReturn(apiResponse);

            // When/Then
            assertThatThrownBy(() -> adapter.customerExists(customerId))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining("Cliente no encontrado");
        }

        @Test
        @DisplayName("Should throw CustomerNotFoundException when data is null")
        void shouldThrowCustomerNotFoundExceptionWhenDataIsNull() {
            // Given
            String customerId = "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f";
            CustomerApiResponse<CustomerExistsResponse> apiResponse = CustomerApiResponse.<CustomerExistsResponse>builder()
                    .success(true)
                    .data(null)
                    .message("No data")
                    .build();
            when(customerServiceClient.customerExists(customerId)).thenReturn(apiResponse);

            // When/Then
            assertThatThrownBy(() -> adapter.customerExists(customerId))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining("Cliente no encontrado");
        }
    }
}
