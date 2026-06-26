package com.veterinary.service;

import com.veterinary.domain.*;
import com.veterinary.domain.enums.EstadoFactura;
import com.veterinary.domain.enums.MetodoPago;
import com.veterinary.dto.FacturaDto;
import com.veterinary.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    private static final AtomicInteger invoiceCounter = new AtomicInteger(0);

    public FacturaService(FacturaRepository facturaRepository, ClienteRepository clienteRepository,
                           VeterinarioRepository veterinarioRepository, CitaRepository citaRepository,
                           ServicioRepository servicioRepository, MedicamentoRepository medicamentoRepository,
                           MovimientoInventarioRepository movimientoInventarioRepository) {
        this.facturaRepository = facturaRepository;
        this.clienteRepository = clienteRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    public FacturaDto toDto(Factura f) {
        FacturaDto dto = new FacturaDto();
        dto.setId(f.getId());
        dto.setNumeroFactura(f.getNumeroFactura());
        dto.setClienteId(f.getCliente().getId());
        dto.setClienteNombre(f.getCliente().getNombre() + " " + f.getCliente().getApellido());
        dto.setVeterinarioId(f.getVeterinario().getId());
        dto.setVeterinarioNombre(f.getVeterinario().getNombre() + " " + f.getVeterinario().getApellido());
        dto.setCitaId(f.getCita() != null ? f.getCita().getId() : null);
        dto.setFechaEmision(f.getFechaEmision());
        dto.setSubtotal(f.getSubtotal());
        dto.setDescuentoTotal(f.getDescuentoTotal());
        dto.setTotal(f.getTotal());
        dto.setTotalPagado(f.getTotalPagado());
        dto.setSaldoPendiente(f.getTotal() - f.getTotalPagado());
        dto.setEstado(f.getEstado().name());
        dto.setFormaPago(f.getFormaPago() != null ? f.getFormaPago().name() : null);
        dto.setObservaciones(f.getObservaciones());

        dto.setDetalles(f.getDetalles().stream().map(d -> {
            FacturaDto.DetalleFacturaDto det = new FacturaDto.DetalleFacturaDto();
            det.setId(d.getId());
            det.setServicioId(d.getServicio() != null ? d.getServicio().getId() : null);
            det.setServicioNombre(d.getServicio() != null ? d.getServicio().getNombre() : null);
            det.setMedicamentoId(d.getMedicamento() != null ? d.getMedicamento().getId() : null);
            det.setMedicamentoNombre(d.getMedicamento() != null ? d.getMedicamento().getNombre() : null);
            det.setTipoItem(d.getTipoItem());
            det.setDescripcionLinea(d.getDescripcionLinea());
            det.setCantidad(d.getCantidad());
            det.setPrecioUnitario(d.getPrecioUnitario());
            det.setDescuentoLinea(d.getDescuentoLinea());
            det.setSubtotal(d.getSubtotal());
            return det;
        }).toList());

        dto.setPagos(f.getPagos().stream().map(p -> {
            FacturaDto.PagoDto pag = new FacturaDto.PagoDto();
            pag.setId(p.getId());
            pag.setMonto(p.getMonto());
            pag.setFechaPago(p.getFechaPago());
            pag.setMetodo(p.getMetodo().name());
            pag.setObservacion(p.getObservacion());
            pag.setReferencia(p.getReferencia());
            return pag;
        }).toList());

        return dto;
    }

    public List<FacturaDto> findAll() {
        return facturaRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<FacturaDto> findByCliente(Long clienteId) {
        return facturaRepository.findByClienteIdOrderByFechaEmisionDesc(clienteId)
                .stream().map(this::toDto).toList();
    }

    public FacturaDto findById(Long id) {
        return toDto(facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada")));
    }

    @Transactional
    public FacturaDto crearFactura(FacturaDto dto, Long veterinarioId) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Veterinario vet = veterinarioRepository.findById(veterinarioId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        Factura factura = new Factura();
        factura.setNumeroFactura(generarNumeroFactura());
        factura.setCliente(cliente);
        factura.setVeterinario(vet);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setObservaciones(dto.getObservaciones());

        double subtotal = 0;
        if (dto.getDetalles() != null) {
            for (FacturaDto.DetalleFacturaDto detDto : dto.getDetalles()) {
                DetalleFactura det = new DetalleFactura();
                det.setFactura(factura);
                det.setTipoItem(detDto.getTipoItem());
                det.setDescripcionLinea(detDto.getDescripcionLinea());
                det.setCantidad(detDto.getCantidad());
                det.setDescuentoLinea(detDto.getDescuentoLinea() != null ? detDto.getDescuentoLinea() : 0);

                if ("SERVICIO".equals(detDto.getTipoItem()) && detDto.getServicioId() != null) {
                    Servicio s = servicioRepository.findById(detDto.getServicioId())
                            .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
                    det.setServicio(s);
                    det.setPrecioUnitario(s.getPrecioBase());
                } else if ("MEDICAMENTO".equals(detDto.getTipoItem()) && detDto.getMedicamentoId() != null) {
                    Medicamento m = medicamentoRepository.findById(detDto.getMedicamentoId())
                            .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));
                    if (m.getStockActual() < detDto.getCantidad()) {
                        throw new RuntimeException("Stock insuficiente de " + m.getNombre());
                    }
                    det.setMedicamento(m);
                    det.setPrecioUnitario(m.getPrecioUnitario());

                    m.setStockActual(m.getStockActual() - detDto.getCantidad());
                    medicamentoRepository.save(m);

                    MovimientoInventario mov = new MovimientoInventario();
                    mov.setMedicamento(m);
                    mov.setTipo(com.veterinary.domain.enums.TipoMovimiento.SALIDA);
                    mov.setCantidad(detDto.getCantidad());
                    mov.setStockResultante(m.getStockActual());
                    mov.setMotivo("Venta - Factura");
                    movimientoInventarioRepository.save(mov);
                } else {
                    det.setPrecioUnitario(detDto.getPrecioUnitario());
                }

                det.setSubtotal((det.getPrecioUnitario() * det.getCantidad()) - det.getDescuentoLinea());
                subtotal += det.getSubtotal();
                factura.getDetalles().add(det);
            }
        }

        factura.setSubtotal(subtotal);
        factura.setDescuentoTotal(dto.getDescuentoTotal() != null ? dto.getDescuentoTotal() : 0);
        factura.setTotal(subtotal - factura.getDescuentoTotal());
        factura.setEstado(EstadoFactura.PENDIENTE);

        return toDto(facturaRepository.save(factura));
    }

    @Transactional
    public FacturaDto registrarPago(Long facturaId, FacturaDto.PagoDto pagoDto) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setMonto(pagoDto.getMonto());
        pago.setFechaPago(LocalDateTime.now());
        pago.setMetodo(MetodoPago.valueOf(pagoDto.getMetodo()));
        pago.setObservacion(pagoDto.getObservacion());
        pago.setReferencia(pagoDto.getReferencia());
        factura.getPagos().add(pago);

        double totalPagado = factura.getTotalPagado() + pagoDto.getMonto();
        factura.setTotalPagado(totalPagado);

        if (totalPagado >= factura.getTotal()) {
            factura.setEstado(EstadoFactura.PAGADA);
        } else if (totalPagado > 0) {
            factura.setEstado(EstadoFactura.PAGADA_PARCIAL);
        }

        return toDto(facturaRepository.save(factura));
    }

    private String generarNumeroFactura() {
        int seq = invoiceCounter.incrementAndGet() % 10000;
        return "FAC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + String.format("%04d", seq);
    }
}
