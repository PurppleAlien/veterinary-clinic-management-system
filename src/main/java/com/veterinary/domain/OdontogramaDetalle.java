package com.veterinary.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "odontograma_detalles")
public class OdontogramaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "odontograma_id", nullable = false)
    private Odontograma odontograma;

    @Column(name = "tooth_number", nullable = false)
    private Integer toothNumber;

    @Column(name = "cuadrante", nullable = false)
    private String cuadrante;

    @Column(nullable = false)
    private String diente;

    @Column(nullable = false)
    private String estado;

    private String observacion;

    @Column(name = "color_hex")
    private String colorHex;
}
