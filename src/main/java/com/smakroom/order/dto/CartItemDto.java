package com.smakroom.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CartItemDto implements Serializable {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private String imageUrl;

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
