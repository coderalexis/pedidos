package com.liverpool.order.infrastructure.adapter.out.client;

import com.liverpool.order.infrastructure.adapter.out.client.config.FeignClientConfig;
import com.liverpool.order.infrastructure.adapter.out.client.dto.CustomerApiResponse;
import com.liverpool.order.infrastructure.adapter.out.client.dto.CustomerExistsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "customer-service",
        url = "${customer-service.url}",
        configuration = FeignClientConfig.class
)
public interface CustomerServiceClient {

    @GetMapping("/internal/api/v1/customers/{customerId}/exists")
    CustomerApiResponse<CustomerExistsResponse> customerExists(@PathVariable("customerId") String customerId);
}
