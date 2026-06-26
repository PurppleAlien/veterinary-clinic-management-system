package com.veterinary.service;

import com.veterinary.repository.FacturaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Service
public class ReporteService {

    private final FacturaRepository facturaRepository;

    public ReporteService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    public Map<String, Object> getIngresos(String desde, String hasta) {
        LocalDateTime inicio = desde != null ? LocalDate.parse(desde).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = hasta != null ? LocalDate.parse(hasta).atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);

        Double total = facturaRepository.sumIngresosByFecha(inicio, fin);
        return Map.of(
                "totalIngresos", total != null ? total : 0.0,
                "desde", inicio.toString(),
                "hasta", fin.toString()
        );
    }

    public byte[] getIngresosExcel(String desde, String hasta) {
        LocalDateTime inicio = desde != null ? LocalDate.parse(desde).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = hasta != null ? LocalDate.parse(hasta).atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);

        var facturas = facturaRepository.findByFechaBetween(inicio, fin);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ingresos");
            Row header = sheet.createRow(0);
            String[] cols = {"N° Factura", "Cliente", "Fecha", "Total", "Estado"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }

            int rowNum = 1;
            for (var f : facturas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(f.getNumeroFactura());
                row.createCell(1).setCellValue(f.getCliente().getNombre() + " " + f.getCliente().getApellido());
                row.createCell(2).setCellValue(f.getFechaEmision().toString());
                row.createCell(3).setCellValue(f.getTotal());
                row.createCell(4).setCellValue(f.getEstado().name());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel", e);
        }
    }
}
