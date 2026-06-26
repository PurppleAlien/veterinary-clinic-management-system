package com.veterinary.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ClienteDto {
    private Long id;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El apellido es requerido")
    private String apellido;

    private LocalDate fechaNacimiento;

    @Email(message = "Formato de email inválido")
    private String email;

    private String telefono;
    private String direccion;
    private String identificacionFiscal;
    private Boolean portalActivo;
    private List<MascotaDto> mascotas;
}
