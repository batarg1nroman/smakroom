package com.smakroom.review.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {

    @NotNull(message = "ID продукта обязателен")
    private Long productId;

    @NotNull(message = "Оценка обязательна")
    @Min(value = 1, message = "Оценка от 1 до 5")
    @Max(value = 5, message = "Оценка от 1 до 5")
    private Integer rating;

    @Size(max = 1000, message = "Комментарий: до 1000 символов")
    private String comment;
}
