package com.sp.parking.tenant.web.dto;

import jakarta.validation.constraints.*;

public final class AuthDtos {
  private AuthDtos() {}
  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
  public record LoginResponse(String token, String userName) {}
}

