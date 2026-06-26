package com.veterinary.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.veterinary.domain.Factura;
import com.veterinary.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    private final FacturaRepository facturaRepository;

    @Value("${app.clinic.nombre}")
    private String clinicNombre;

    @Value("${app.clinic.direccion}")
    private String clinicDireccion;

    @Value("${app.clinic.telefono}")
    private String clinicTelefono;

    @Value("${app.clinic.email}")
    private String clinicEmail;

    public PdfService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    public byte[] generateInvoicePdf(Long facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdfDoc);

        document.add(new Paragraph(clinicNombre).setBold().setFontSize(20));
        document.add(new Paragraph(clinicDireccion));
        document.add(new Paragraph("Tel: " + clinicTelefono + " | Email: " + clinicEmail));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("FACTURA #" + factura.getNumeroFactura()).setBold().setFontSize(16));
        document.add(new Paragraph("Cliente: " + factura.getCliente().getNombre() + " " + factura.getCliente().getApellido()));
        document.add(new Paragraph("Fecha: " + factura.getFechaEmision().toString()));
        document.add(new Paragraph(" "));

        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(new Cell().add(new Paragraph("Descripción").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Precio Unit.").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Subtotal").setBold()));

        for (var detalle : factura.getDetalles()) {
            table.addCell(new Cell().add(new Paragraph(detalle.getDescripcionLinea())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(detalle.getCantidad()))));
            table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", detalle.getPrecioUnitario()))));
            table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", detalle.getSubtotal()))));
        }

        document.add(table);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Subtotal: $" + String.format("%.2f", factura.getSubtotal())));
        document.add(new Paragraph("Descuento: $" + String.format("%.2f", factura.getDescuentoTotal())));
        document.add(new Paragraph("TOTAL: $" + String.format("%.2f", factura.getTotal())).setBold().setFontSize(14));
        document.add(new Paragraph("Pagado: $" + String.format("%.2f", factura.getTotalPagado())));
        document.add(new Paragraph("Saldo: $" + String.format("%.2f", factura.getTotal() - factura.getTotalPagado())));

        document.close();
        return baos.toByteArray();
    }
}
