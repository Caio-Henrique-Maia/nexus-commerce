package com.nexus.commerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "Please inform a name")
        @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
        String name,
        @NotBlank(message = "Please inform an e-mail")
        @Email(message = "Please inform a valid e-mail")
        @Size(max = 255, message = "E-mail must contain at most 255 characters")
        String email
) { }