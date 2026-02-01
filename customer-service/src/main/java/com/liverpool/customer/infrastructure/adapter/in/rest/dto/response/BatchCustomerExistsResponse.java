package com.liverpool.customer.infrastructure.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCustomerExistsResponse {
    private Map<String, Boolean> results;
    private int totalRequested;
    private int totalExisting;
}
