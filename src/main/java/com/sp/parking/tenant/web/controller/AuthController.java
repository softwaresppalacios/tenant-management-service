package com.sp.parking.tenant.web.controller;

import com.sp.parking.tenant.web.dto.AuthDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    if ("admin@spparqueaderos.com".equalsIgnoreCase(request.email()) && "admin123".equals(request.password())) {
      return new LoginResponse("demo-token", "Administrador Principal");
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }
}

