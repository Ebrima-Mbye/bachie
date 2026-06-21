package com.ecommerce.dto.order;

import com.ecommerce.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}
