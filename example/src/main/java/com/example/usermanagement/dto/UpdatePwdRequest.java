package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePwdRequest(
    @NotBlank @Size(min = 6) String pwd
) {}
