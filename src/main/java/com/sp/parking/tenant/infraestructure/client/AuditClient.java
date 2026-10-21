package com.sp.parking.tenant.infraestructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reporta acciones del usuario a reporting-audit-service. Se envia en segundo plano y despues del commit:
 * un fallo de auditoria nunca bloquea ni revierte la operacion del parqueadero.
 */
@Slf4j
@Component
public class AuditClient {
  private static final ExecutorService SENDER = Executors.newSingleThreadExecutor(runnable -> {
    Thread thread = new Thread(runnable, "audit-sender");
    thread.setDaemon(true);
    return thread;
  });

  private final RestClient restClient;
  private final String source;

  public AuditClient(
    @Value("${services.audit.base-url:http://localhost:8085/api}") String baseUrl,
    @Value("${spring.application.name}") String source
  ) {
    SimpleClientHttpRequestFactory timeouts = new SimpleClientHttpRequestFactory();
    timeouts.setConnectTimeout(3_000);
    timeouts.setReadTimeout(5_000);
    this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(timeouts).build();
    this.source = source;
  }

  /** authorization: encabezado Authorization del usuario; el servicio de auditoria toma de ahi quien hizo la accion. */
  public void record(UUID tenantId, String authorization, String action, String entityType, String entityRef, String details) {
    if (tenantId == null || authorization == null) return;
    AuditEvent event = new AuditEvent(action, entityType, entityRef, details, source);
    Runnable send = () -> SENDER.execute(() -> post(tenantId, authorization, event));
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override public void afterCommit() { send.run(); }
      });
    } else {
      send.run();
    }
  }

  public static String money(BigDecimal amount) {
    NumberFormat format = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));
    return "$" + format.format(amount == null ? BigDecimal.ZERO : amount);
  }

  private void post(UUID tenantId, String authorization, AuditEvent event) {
    try {
      restClient.post()
        .uri("/audit")
        .header("X-Tenant-Id", tenantId.toString())
        .header(HttpHeaders.AUTHORIZATION, authorization)
        .contentType(MediaType.APPLICATION_JSON)
        .body(event)
        .retrieve()
        .toBodilessEntity();
    } catch (RuntimeException ex) {
      log.warn("No se pudo registrar la auditoría {} {}: {}", event.action(), event.entityRef(), ex.getMessage());
    }
  }

  record AuditEvent(String action, String entityType, String entityRef, String details, String source) {}
}
