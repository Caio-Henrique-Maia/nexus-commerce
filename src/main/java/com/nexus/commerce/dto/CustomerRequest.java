package com.nexus.commerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "Please inform a name")
        @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
        String name,

        @NotBlank(message = "Please inform a e-mail")
        @Email(message = "Please inform a valid e-mail")
        String email
) {
}
