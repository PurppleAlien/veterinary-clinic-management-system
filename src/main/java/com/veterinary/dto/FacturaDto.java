package com.veterinary.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FacturaDto {
    private Long id;
    private String numeroFactura;

    @NotNull(message = "El cliente es requerido")
    private Long clienteId;

    private String clienteNombre;
    private Long veterinarioId;
    private String veterinarioNombre;
    private Long citaId;
    private LocalDateTime fechaEmision;
    private Double subtotal;
    private Double descuentoTotal;
    private Double total;
    private Double totalPagado;
    private Double saldoPendiente;
    private String estado;
    private String formaPago;
    private String observaciones;
    private List<DetalleFacturaDto> detalles;
    private List<PagoDto> pagos;

    @Data
    public static class DetalleFacturaDto {
        private Long id;
        private Long servicioId;
        private String servicioNombre;
        private Long medicamentoId;
        private String medicamentoNombre;
        private String tipoItem;
        private String descripcionLinea;

        @NotNull(message = "La cantidad es requerida")
        private Integer cantidad;

        private Double precioUnitario;
        private Double descuentoLinea;
        private Double subtotal;
    }

    @Data
    public static class PagoDto {
        private Long id;

        @NotNull(message = "El monto es requerido")
        private Double monto;

        private LocalDateTime fechaPago;

        @NotNull(message = "El método de pago es requerido")
        private String metodo;

        private String observacion;
        private String referencia;
    }
}
