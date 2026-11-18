package com.sp.parking.tenant.web.controller;

import com.sp.parking.tenant.application.service.AuthService;
import com.sp.parking.tenant.web.dto.AuthDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @GetMapping("/me")
  public MeResponse me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
    return authService.me(bearerToken(authorization));
  }

  @PostMapping("/change-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @Valid @RequestBody ChangePasswordRequest request
  ) {
    authService.changePassword(bearerToken(authorization), request);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
    authService.logout(bearerToken(authorization));
  }

  public static String bearerToken(String authorization) {
    if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
    return authorization.substring(7).trim();
  }
}
