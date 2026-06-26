package com.veterinary.controller;

import com.veterinary.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/medico/reportes")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping("/ingresos")
    public ResponseEntity<?> ingresos(@RequestParam(required = false) String desde,
                                       @RequestParam(required = false) String hasta) {
        Map<String, Object> data = service.getIngresos(desde, hasta);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/ingresos/excel")
    public ResponseEntity<byte[]> ingresosExcel(@RequestParam(required = false) String desde,
                                                  @RequestParam(required = false) String hasta) {
        byte[] excel = service.getIngresosExcel(desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ingresos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/auditoria")
    public ResponseEntity<?> auditoria() {
        return ResponseEntity.ok(service.getIngresos(null, null));
    }
}
