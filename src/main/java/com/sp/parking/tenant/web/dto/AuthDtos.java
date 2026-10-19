package com.sp.parking.tenant.web.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AuthDtos {
  private AuthDtos() {}

  public record LoginRequest(
    @NotBlank(message = "Ingresa tu correo") @Email(message = "El correo no es válido") String email,
    @NotBlank(message = "Ingresa tu contraseña") String password
  ) {}

  public record RegisterRequest(
    @NotBlank(message = "Ingresa tu nombre") @Size(max = 120, message = "El nombre es demasiado largo") String fullName,
    @NotBlank(message = "Ingresa tu correo") @Email(message = "El correo no es válido") @Size(max = 160) String email,
    @NotBlank(message = "Ingresa una contraseña")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe tener letras y números")
    String password,
    @NotBlank(message = "Ingresa el nombre del parqueadero") @Size(max = 120, message = "El nombre del parqueadero es demasiado largo") String parkingName,
    @Size(max = 200) String branchAddress
  ) {}

  public record UserResponse(UUID uuid, String fullName, String email) {}
  public record TenantAccess(UUID uuid, String name, String role, String status, String planName, int branchLimit, int branchCount) {}
  public record AuthResponse(String token, Instant expiresAt, UserResponse user, List<TenantAccess> tenants) {}
  public record MeResponse(UserResponse user, List<TenantAccess> tenants) {}
}
