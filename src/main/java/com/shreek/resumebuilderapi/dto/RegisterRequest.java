package com.shreek.resumebuilderapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @Email(message = "Email Should be Valid")
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Name is Required")
    @Size(min = 2, max = 15,message = "Name must contain the characters between 2 to 15")
    private String name;
    @NotBlank(message = "Password is Required")
    @Size(min = 6,max = 15,message="the password must contain 6 characters and maximum of 15")
    private String password;
    private String profileImageUrl;
}
