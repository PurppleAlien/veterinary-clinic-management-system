package com.veterinary.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CitaDto {
    private Long id;

    @NotNull(message = "La mascota es requerida")
    private Long mascotaId;

    private String mascotaNombre;
    private String especie;

    private Long clienteId;
    private String clienteNombre;
    private Long veterinarioId;
    private String veterinarioNombre;

    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDateTime fechaHoraInicio;

    @NotNull(message = "La fecha de fin es requerida")
    private LocalDateTime fechaHoraFin;

    @NotBlank(message = "El motivo es requerido")
    private String motivo;

    private String notas;

    @NotBlank(message = "El estado es requerido")
    private String estado;

    private String motivoCancelacion;
}
