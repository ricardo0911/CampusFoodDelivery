package com.campus.ordering.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO {
    private Long shopId;
    private Long addressId;
    private Long couponId;
    private String remark;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long dishId;
        private Integer quantity;
        private String specifications;
        private BigDecimal unitPrice;
    }
}
