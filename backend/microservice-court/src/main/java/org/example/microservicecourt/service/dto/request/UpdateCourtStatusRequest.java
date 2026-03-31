package org.example.microservicecourt.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourtStatusRequest {

  @NotNull(message = "El estado activo/inactivo es requerido")
  private Boolean isActive;

  private String maintenanceNotes;

  private String estimatedRepairDate;

  // Campos adicionales útiles
  private Boolean temporaryClosure;
  private String closureReason;
  private LocalDateTime expectedReopening;

  // Método para validar la solicitud
  public void validate() {
    if (isActive == null) {
      throw new IllegalArgumentException("El campo 'isActive' es requerido");
    }

    // Si se está desactivando la cancha y es por mantenimiento,
    // las notas de mantenimiento son recomendadas
    if (Boolean.FALSE.equals(isActive) && (maintenanceNotes == null || maintenanceNotes.trim().isEmpty())) {
      log.warn("Se está desactivando una cancha sin especificar motivo. Considera agregar notas de mantenimiento.");
    }
  }

  // Método para obtener descripción del estado
  public String getStatusDescription() {
    if (Boolean.TRUE.equals(isActive)) {
      return "ACTIVA";
    } else if (maintenanceNotes != null && !maintenanceNotes.trim().isEmpty()) {
      return "EN MANTENIMIENTO: " + maintenanceNotes;
    } else if (Boolean.TRUE.equals(temporaryClosure)) {
      return "CERRADA TEMPORALMENTE: " + (closureReason != null ? closureReason : "Sin razón especificada");
    } else {
      return "INACTIVA";
    }
  }
}