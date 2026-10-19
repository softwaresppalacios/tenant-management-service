package com.sp.parking.tenant.web.controller;

import com.sp.parking.tenant.application.service.MemberService;
import com.sp.parking.tenant.web.dto.MemberDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants/{tenantUuid}/members")
@RequiredArgsConstructor
public class MemberController {
  private final MemberService memberService;

  @GetMapping
  public List<MemberResponse> list(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @PathVariable("tenantUuid") UUID tenantUuid
  ) {
    return memberService.list(AuthController.bearerToken(authorization), tenantUuid);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MemberCreatedResponse create(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @PathVariable("tenantUuid") UUID tenantUuid,
    @Valid @RequestBody CreateMemberRequest request
  ) {
    return memberService.create(AuthController.bearerToken(authorization), tenantUuid, request);
  }

  @PutMapping("/{userUuid}")
  public MemberResponse updateRole(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @PathVariable("tenantUuid") UUID tenantUuid,
    @PathVariable("userUuid") UUID userUuid,
    @Valid @RequestBody UpdateMemberRequest request
  ) {
    return memberService.updateRole(AuthController.bearerToken(authorization), tenantUuid, userUuid, request);
  }

  @DeleteMapping("/{userUuid}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @PathVariable("tenantUuid") UUID tenantUuid,
    @PathVariable("userUuid") UUID userUuid
  ) {
    memberService.remove(AuthController.bearerToken(authorization), tenantUuid, userUuid);
  }

  @PostMapping("/{userUuid}/reset-password")
  public MemberResponse resetPassword(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @PathVariable("tenantUuid") UUID tenantUuid,
    @PathVariable("userUuid") UUID userUuid,
    @Valid @RequestBody ResetPasswordRequest request
  ) {
    return memberService.resetPassword(AuthController.bearerToken(authorization), tenantUuid, userUuid, request);
  }
}
