package com.veterinary.service;

import com.veterinary.domain.Odontograma;
import com.veterinary.domain.OdontogramaDetalle;
import com.veterinary.repository.OdontogramaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class OdontogramaService {

    private final OdontogramaRepository repository;

    public OdontogramaService(OdontogramaRepository repository) {
        this.repository = repository;
    }

    public List<Odontograma> findByMascota(Long mascotaId) {
        return repository.findByMascotaIdOrderByFechaDesc(mascotaId);
    }

    public Odontograma findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Odontograma no encontrado"));
    }

    @Transactional
    public Odontograma create(Long mascotaId, Map<String, Object> body, String vetName) {
        Odontograma o = new Odontograma();
        o.setMascotaId(mascotaId);
        String fechaStr = (String) body.get("fecha");
        o.setFecha(fechaStr != null ? java.time.LocalDate.parse(fechaStr) : java.time.LocalDate.now());
        o.setNotas((String) body.get("notas"));
        o.setVeterinarioNombre(vetName);
        applyDetalles(o, body);
        return repository.save(o);
    }

    @Transactional
    public Odontograma update(Long id, Map<String, Object> body) {
        Odontograma o = findById(id);
        if (body.containsKey("notas")) o.setNotas((String) body.get("notas"));
        if (body.containsKey("detalles")) {
            o.getDetalles().clear();
            applyDetalles(o, body);
        }
        return repository.save(o);
    }

    @SuppressWarnings("unchecked")
    private void applyDetalles(Odontograma o, Map<String, Object> body) {
        List<Map<String, Object>> detalles = (List<Map<String, Object>>) body.get("detalles");
        if (detalles == null) return;
        for (Map<String, Object> d : detalles) {
            OdontogramaDetalle det = new OdontogramaDetalle();
            det.setOdontograma(o);
            det.setToothNumber(d.get("toothNumber") != null ? ((Number) d.get("toothNumber")).intValue() : 0);
            det.setCuadrante((String) d.get("cuadrante"));
            det.setDiente((String) d.get("diente"));
            det.setEstado((String) d.get("estado"));
            det.setObservacion((String) d.get("observacion"));
            det.setColorHex((String) d.get("colorHex"));
            o.getDetalles().add(det);
        }
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
