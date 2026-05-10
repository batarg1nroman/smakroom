package com.smakroom.order.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutDto {

    @Size(max = 500, message = "Комментарий: до 500 символов")
    private String notes;
}
