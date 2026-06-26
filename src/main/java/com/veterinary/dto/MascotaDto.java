package com.veterinary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MascotaDto {
    private Long id;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "La especie es requerida")
    private String especie;

    private String raza;
    private String color;
    private String genero;
    private LocalDate fechaNacimiento;
    private Double peso;
    private String alergias;
    private String condicionesMedicas;

    @NotNull(message = "El cliente es requerido")
    private Long clienteId;

    private String clienteNombre;
}
