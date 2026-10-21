package com.sp.parking.tenant.web.controller;

import com.sp.parking.tenant.infraestructure.client.AuditClient;
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
  private final AuditClient auditClient;

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
    MemberCreatedResponse created = memberService.create(AuthController.bearerToken(authorization), tenantUuid, request);
    audit(tenantUuid, authorization, "MEMBER_ADD", created.member(), "Rol " + created.member().role() + (created.existingAccount() ? " · cuenta existente" : " · cuenta nueva con clave temporal"));
    return created;
  }

  @PutMapping("/{userUuid}")
  public MemberResponse updateRole(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @PathVariable("tenantUuid") UUID tenantUuid,
    @PathVariable("userUuid") UUID userUuid,
    @Valid @RequestBody UpdateMemberRequest request
  ) {
    MemberResponse updated = memberService.updateRole(AuthController.bearerToken(authorization), tenantUuid, userUuid, request);
    audit(tenantUuid, authorization, "MEMBER_UPDATE", updated, "Rol " + updated.role() + (updated.active() ? "" : " · desactivado"));
    return updated;
  }

  @DeleteMapping("/{userUuid}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @PathVariable("tenantUuid") UUID tenantUuid,
    @PathVariable("userUuid") UUID userUuid
  ) {
    String token = AuthController.bearerToken(authorization);
    MemberResponse removed = memberService.list(token, tenantUuid).stream().filter(member -> member.uuid().equals(userUuid)).findFirst().orElse(null);
    memberService.remove(token, tenantUuid, userUuid);
    if (removed != null) audit(tenantUuid, authorization, "MEMBER_REMOVE", removed, "Rol " + removed.role() + " · acceso retirado");
  }

  @PostMapping("/{userUuid}/reset-password")
  public MemberResponse resetPassword(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @PathVariable("tenantUuid") UUID tenantUuid,
    @PathVariable("userUuid") UUID userUuid,
    @Valid @RequestBody ResetPasswordRequest request
  ) {
    MemberResponse reset = memberService.resetPassword(AuthController.bearerToken(authorization), tenantUuid, userUuid, request);
    audit(tenantUuid, authorization, "MEMBER_RESET_PASSWORD", reset, "Clave temporal asignada");
    return reset;
  }

  private void audit(UUID tenantUuid, String authorization, String action, MemberResponse member, String details) {
    auditClient.record(tenantUuid, authorization, action, "MEMBER", member.fullName() + " <" + member.email() + ">", details);
  }
}
