package com.sp.parking.tenant.application.service;

import com.sp.parking.tenant.domain.model.*;
import com.sp.parking.tenant.domain.repository.*;
import com.sp.parking.tenant.web.dto.AuthDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService {
  private static final String INVALID_CREDENTIALS = "Correo o contraseña incorrectos";
  private static final SecureRandom RANDOM = new SecureRandom();

  private final UserAccountRepository userRepository;
  private final TenantMembershipRepository membershipRepository;
  private final UserSessionRepository sessionRepository;
  private final TenantService tenantService;
  private final PasswordEncoder passwordEncoder;

  @Value("${auth.session-hours:12}")
  private long sessionHours;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    if (userRepository.existsByEmail(email)) {
      throw new ResponseStatusException(CONFLICT, "Ya existe una cuenta con ese correo");
    }
    UserAccount user = userRepository.save(UserAccount.builder()
      .fullName(request.fullName().trim())
      .email(email)
      .passwordHash(passwordEncoder.encode(request.password()))
      .active(true)
      .build());
    Tenant tenant = tenantService.createTenant(request.parkingName(), request.branchAddress());
    grantAccess(user, tenant, MembershipRole.OWNER);
    return openSession(user);
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    UserAccount user = userRepository.findByEmail(normalizeEmail(request.email()))
      .filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
      .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, INVALID_CREDENTIALS));
    if (!user.isActive()) {
      throw new ResponseStatusException(FORBIDDEN, "Tu cuenta está desactivada");
    }
    user.setLastLoginAt(Instant.now());
    return openSession(user);
  }

  @Transactional(readOnly = true)
  public MeResponse me(String token) {
    UserAccount user = requireSession(token).getUser();
    return new MeResponse(toUser(user), tenantsOf(user));
  }

  @Transactional
  public void logout(String token) {
    findSession(token).ifPresent(session -> {
      if (session.getRevokedAt() == null) session.setRevokedAt(Instant.now());
    });
  }

  /** Devuelve el usuario de un token valido, o vacio si no hay token. Lanza 401 si el token no sirve. */
  @Transactional(readOnly = true)
  public Optional<UserAccount> optionalUser(String token) {
    if (token == null || token.isBlank()) return Optional.empty();
    return Optional.of(requireSession(token).getUser());
  }

  @Transactional
  public void grantAccess(UserAccount user, Tenant tenant, MembershipRole role) {
    membershipRepository.save(TenantMembership.builder().user(user).tenant(tenant).role(role).build());
  }

  private AuthResponse openSession(UserAccount user) {
    String token = newToken();
    Instant now = Instant.now();
    UserSession session = sessionRepository.save(UserSession.builder()
      .tokenHash(hash(token))
      .user(user)
      .createdAt(now)
      .expiresAt(now.plus(Duration.ofHours(sessionHours)))
      .build());
    return new AuthResponse(token, session.getExpiresAt(), toUser(user), tenantsOf(user));
  }

  private UserSession requireSession(String token) {
    if (token == null || token.isBlank()) {
      throw new ResponseStatusException(UNAUTHORIZED, "Inicia sesión para continuar");
    }
    UserSession session = findSession(token)
      .filter(item -> item.isValid(Instant.now()))
      .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Tu sesión expiró, vuelve a iniciar sesión"));
    if (!session.getUser().isActive()) {
      throw new ResponseStatusException(FORBIDDEN, "Tu cuenta está desactivada");
    }
    return session;
  }

  private Optional<UserSession> findSession(String token) {
    if (token == null || token.isBlank()) return Optional.empty();
    return sessionRepository.findByTokenHashWithUser(hash(token));
  }

  private List<TenantAccess> tenantsOf(UserAccount user) {
    return membershipRepository.findByUserIdWithTenant(user.getId()).stream()
      .map(membership -> {
        Tenant tenant = membership.getTenant();
        return new TenantAccess(
          tenant.getUuid(),
          tenant.getName(),
          membership.getRole().name(),
          tenant.getStatus().name(),
          tenant.getPlan().getName(),
          tenant.getPlan().getBranchLimit(),
          tenant.getBranches().size()
        );
      })
      .toList();
  }

  private UserResponse toUser(UserAccount user) {
    return new UserResponse(user.getUuid(), user.getFullName(), user.getEmail());
  }

  private String normalizeEmail(String email) {
    return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
  }

  private String newToken() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hash(String token) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 no disponible", ex);
    }
  }
}
