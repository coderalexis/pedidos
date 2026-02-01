package com.liverpool.customer.domain.port.in;

import com.liverpool.customer.domain.model.Customer;

import java.util.List;
import java.util.Map;

public interface ValidateCustomerExistsUseCase {
    boolean customerExists(String id);
    Customer getBasicCustomerInfo(String id);
    Map<String, Boolean> validateMultipleCustomers(List<String> ids);
}
