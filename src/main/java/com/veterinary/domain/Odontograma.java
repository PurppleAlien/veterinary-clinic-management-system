package com.veterinary.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "odontogramas")
public class Odontograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mascota_id", nullable = false)
    private Long mascotaId;

    @Column(nullable = false)
    private LocalDate fecha;

    private String notas;

    @Column(name = "veterinario_nombre")
    private String veterinarioNombre;

    @OneToMany(mappedBy = "odontograma", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OdontogramaDetalle> detalles = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
