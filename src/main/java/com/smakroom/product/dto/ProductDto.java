package com.smakroom.product.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductDto {

    @NotBlank(message = "Название обязательно")
    @Size(max = 150, message = "Название: до 150 символов")
    private String name;

    @NotBlank(message = "Описание обязательно")
    private String description;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    @Digits(integer = 8, fraction = 2, message = "Некорректный формат цены")
    private BigDecimal price;

    private String imageUrl;

    @NotNull(message = "Категория обязательна")
    private Long categoryId;

    private boolean available = true;

    @Min(value = 0, message = "Остаток не может быть отрицательным")
    private int stock;
}
