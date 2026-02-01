package com.liverpool.order.domain.port.out;

public interface CustomerValidationPort {
    boolean customerExists(String customerId);
}
