package com.clinic.identity.web;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "请填写账号") String username,
        @NotBlank(message = "请填写密码") String password
) {
}
