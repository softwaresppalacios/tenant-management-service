package com.sp.parking.tenant.web.dto;

import com.sp.parking.tenant.domain.model.MembershipRole;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public final class MemberDtos {
  private MemberDtos() {}

  /** La clave temporal solo se usa si el correo aun no tiene cuenta. */
  public record CreateMemberRequest(
    @NotBlank(message = "Ingresa el nombre") @Size(max = 120, message = "El nombre es demasiado largo") String fullName,
    @NotBlank(message = "Ingresa el correo") @Email(message = "El correo no es válido") @Size(max = 160) String email,
    @NotNull(message = "Selecciona el rol") MembershipRole role,
    @Size(min = 8, max = 72, message = "La contraseña temporal debe tener entre 8 y 72 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña temporal debe tener letras y números")
    String temporaryPassword
  ) {}

  public record UpdateMemberRequest(@NotNull(message = "Selecciona el rol") MembershipRole role) {}

  public record ResetPasswordRequest(
    @NotBlank(message = "Ingresa la contraseña temporal")
    @Size(min = 8, max = 72, message = "La contraseña temporal debe tener entre 8 y 72 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña temporal debe tener letras y números")
    String temporaryPassword
  ) {}

  public record MemberResponse(
    UUID uuid,
    String fullName,
    String email,
    String role,
    boolean active,
    boolean mustChangePassword,
    Instant lastLoginAt,
    Instant createdAt
  ) {}

  /** existingAccount: el correo ya tenia cuenta y conserva su contraseña. */
  public record MemberCreatedResponse(MemberResponse member, boolean existingAccount) {}
}
