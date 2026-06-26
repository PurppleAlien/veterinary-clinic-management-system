package com.veterinary.service;

import com.veterinary.domain.Medicamento;
import com.veterinary.domain.MovimientoInventario;
import com.veterinary.domain.enums.TipoMovimiento;
import com.veterinary.repository.MedicamentoRepository;
import com.veterinary.repository.MovimientoInventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class MedicamentoService {

    private final MedicamentoRepository repository;
    private final MovimientoInventarioRepository movimientoRepository;

    public MedicamentoService(MedicamentoRepository repository, MovimientoInventarioRepository movimientoRepository) {
        this.repository = repository;
        this.movimientoRepository = movimientoRepository;
    }

    public List<Medicamento> findAll() {
        return repository.findByActivoTrue();
    }

    @Transactional
    public Medicamento create(Medicamento m) {
        m.setActivo(true);
        return repository.save(m);
    }

    @Transactional
    public Medicamento update(Long id, Medicamento m) {
        Medicamento existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));
        if (m.getNombre() != null) existing.setNombre(m.getNombre());
        if (m.getDescripcion() != null) existing.setDescripcion(m.getDescripcion());
        if (m.getPrecioUnitario() != null) existing.setPrecioUnitario(m.getPrecioUnitario());
        if (m.getStockMinimo() != null) existing.setStockMinimo(m.getStockMinimo());
        if (m.getUnidad() != null) existing.setUnidad(m.getUnidad());
        return repository.save(existing);
    }

    @Transactional
    public Medicamento adjustStock(Long id, Map<String, Object> body) {
        Medicamento m = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));
        int cantidad = (int) body.get("cantidad");
        String tipo = (String) body.get("tipo");

        MovimientoInventario mov = new MovimientoInventario();
        mov.setMedicamento(m);
        mov.setCantidad(Math.abs(cantidad));
        mov.setTipo(TipoMovimiento.valueOf(tipo));
        mov.setMotivo((String) body.get("motivo"));

        if (tipo.equals("ENTRADA")) {
            m.setStockActual(m.getStockActual() + Math.abs(cantidad));
        } else if (tipo.equals("SALIDA")) {
            if (m.getStockActual() < Math.abs(cantidad)) {
                throw new RuntimeException("Stock insuficiente");
            }
            m.setStockActual(m.getStockActual() - Math.abs(cantidad));
        } else {
            m.setStockActual(m.getStockActual() + cantidad);
        }

        mov.setStockResultante(m.getStockActual());
        repository.save(m);
        movimientoRepository.save(mov);
        return m;
    }

    public List<Medicamento> stockBajo() {
        return repository.findByStockActualLessThanEqual(5);
    }

    @Transactional
    public void delete(Long id) {
        Medicamento m = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));
        m.setActivo(false);
        repository.save(m);
    }
}
