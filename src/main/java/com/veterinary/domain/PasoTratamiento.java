package com.veterinary.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.veterinary.domain.enums.EstadoPaso;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "pasos_tratamiento")
public class PasoTratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnoreProperties({"pasos"})
    private PlanTratamiento planTratamiento;

    @Column(nullable = false)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPaso estado = EstadoPaso.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String notas;
}
