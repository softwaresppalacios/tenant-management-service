package com.sp.parking.tenant.application.service;

import com.sp.parking.tenant.domain.model.*;
import com.sp.parking.tenant.domain.repository.*;
import com.sp.parking.tenant.web.dto.MemberDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Usuarios de un parqueadero. Solo el propietario los gestiona: crea cuentas con clave temporal
 * para admins y cajeros, cambia su rol, les quita el acceso o les restablece la clave.
 */
@Service
@RequiredArgsConstructor
public class MemberService {
  private final AuthService authService;
  private final TenantRepository tenantRepository;
  private final TenantMembershipRepository membershipRepository;
  private final UserAccountRepository userRepository;
  private final UserSessionRepository sessionRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public List<MemberResponse> list(String token, UUID tenantUuid) {
    requireOwner(token, tenantUuid);
    return membershipRepository.findByTenantUuidWithUser(tenantUuid).stream()
      .sorted(Comparator.comparing(TenantMembership::getRole).thenComparing(m -> m.getUser().getFullName(), String.CASE_INSENSITIVE_ORDER))
      .map(this::toResponse)
      .toList();
  }

  @Transactional
  public MemberCreatedResponse create(String token, UUID tenantUuid, CreateMemberRequest request) {
    requireOwner(token, tenantUuid);
    requireAssignable(request.role());
    Tenant tenant = tenantRepository.findByUuid(tenantUuid)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Parqueadero no encontrado"));
    String email = AuthService.normalizeEmail(request.email());

    UserAccount existing = userRepository.findByEmail(email).orElse(null);
    UserAccount user = existing;
    if (existing != null) {
      if (membershipRepository.findByUserIdAndTenantUuid(existing.getId(), tenantUuid).isPresent()) {
        throw new ResponseStatusException(CONFLICT, "Ese usuario ya tiene acceso a este parqueadero");
      }
    } else {
      if (request.temporaryPassword() == null || request.temporaryPassword().isBlank()) {
        throw new ResponseStatusException(BAD_REQUEST, "Ingresa la contraseña temporal");
      }
      user = userRepository.save(UserAccount.builder()
        .fullName(request.fullName().trim())
        .email(email)
        .passwordHash(passwordEncoder.encode(request.temporaryPassword()))
        .active(true)
        .mustChangePassword(true)
        .build());
    }
    TenantMembership membership = membershipRepository.save(
      TenantMembership.builder().user(user).tenant(tenant).role(request.role()).build()
    );
    return new MemberCreatedResponse(toResponse(membership), existing != null);
  }

  @Transactional
  public MemberResponse updateRole(String token, UUID tenantUuid, UUID userUuid, UpdateMemberRequest request) {
    requireOwner(token, tenantUuid);
    requireAssignable(request.role());
    TenantMembership membership = editableMembership(tenantUuid, userUuid);
    membership.setRole(request.role());
    return toResponse(membership);
  }

  @Transactional
  public void remove(String token, UUID tenantUuid, UUID userUuid) {
    requireOwner(token, tenantUuid);
    membershipRepository.delete(editableMembership(tenantUuid, userUuid));
  }

  /** Asigna una nueva clave temporal y cierra las sesiones del usuario. */
  @Transactional
  public MemberResponse resetPassword(String token, UUID tenantUuid, UUID userUuid, ResetPasswordRequest request) {
    requireOwner(token, tenantUuid);
    TenantMembership membership = editableMembership(tenantUuid, userUuid);
    UserAccount user = membership.getUser();
    // Si trabaja tambien en otro parqueadero, la cuenta no es solo de este negocio.
    if (membershipRepository.countByUserId(user.getId()) > 1) {
      throw new ResponseStatusException(FORBIDDEN, "Este usuario tiene acceso a otros parqueaderos; debe cambiar su contraseña él mismo");
    }
    user.setPasswordHash(passwordEncoder.encode(request.temporaryPassword()));
    user.setMustChangePassword(true);
    sessionRepository.revokeAll(user.getId(), Instant.now());
    return toResponse(membership);
  }

  private void requireOwner(String token, UUID tenantUuid) {
    authService.requireRole(token, tenantUuid, MembershipRole.OWNER);
  }

  private void requireAssignable(MembershipRole role) {
    if (role == MembershipRole.OWNER) {
      throw new ResponseStatusException(BAD_REQUEST, "Solo puedes asignar los roles de administrador o cajero");
    }
  }

  /** La membresia de otro usuario del parqueadero; la del propietario no se puede modificar. */
  private TenantMembership editableMembership(UUID tenantUuid, UUID userUuid) {
    TenantMembership membership = membershipRepository.findByTenantUuidAndUserUuid(tenantUuid, userUuid)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ese usuario no pertenece a este parqueadero"));
    if (membership.getRole() == MembershipRole.OWNER) {
      throw new ResponseStatusException(BAD_REQUEST, "El propietario del parqueadero no se puede modificar");
    }
    return membership;
  }

  private MemberResponse toResponse(TenantMembership membership) {
    UserAccount user = membership.getUser();
    return new MemberResponse(
      user.getUuid(),
      user.getFullName(),
      user.getEmail(),
      membership.getRole().name(),
      user.isActive(),
      user.isMustChangePassword(),
      user.getLastLoginAt(),
      membership.getCreatedAt()
    );
  }
}
