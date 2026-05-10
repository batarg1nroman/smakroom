package com.smakroom.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileDto {

    @Size(max = 50, message = "Имя: до 50 символов")
    private String firstName;

    @Size(max = 50, message = "Фамилия: до 50 символов")
    private String lastName;

    @Size(max = 20, message = "Телефон: до 20 символов")
    private String phone;
}
