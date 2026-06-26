package com.veterinary.service;

import com.veterinary.domain.*;
import com.veterinary.repository.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class NotificacionService {

    private final EmailService emailService;
    private final CitaRepository citaRepository;
    private final VacunaRepository vacunaRepository;
    private final FacturaRepository facturaRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public NotificacionService(EmailService emailService, CitaRepository citaRepository,
                                VacunaRepository vacunaRepository, FacturaRepository facturaRepository) {
        this.emailService = emailService;
        this.citaRepository = citaRepository;
        this.vacunaRepository = vacunaRepository;
        this.facturaRepository = facturaRepository;
    }

    public void recordatorioCita(Long citaId) {
        Cita c = citaRepository.findById(citaId).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        Cliente cl = c.getCliente();
        Mascota m = c.getMascota();
        Veterinario v = c.getVeterinario();

        String to = cl.getPortalEmail() != null ? cl.getPortalEmail() : cl.getEmail();
        if (to == null || to.isBlank()) return;

        String subject = "Recordatorio de Cita - PetClinic Veterinary Clinic";
        String html = """
        <!DOCTYPE html>
        <html><head><meta charset="UTF-8"><style>
            body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}
            .container{max-width:600px;margin:auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)}
            .header{background:#2c3e50;color:#fff;padding:20px;text-align:center}
            .header h1{margin:0;font-size:20px}
            .body{padding:24px}
            .info-table{width:100%%;border-collapse:collapse}
            .info-table td{padding:8px 0;border-bottom:1px solid #eee}
            .info-table td:first-child{font-weight:700;color:#555;width:120px}
            .footer{background:#f8f9fa;padding:16px;text-align:center;font-size:12px;color:#888}
            .badge{display:inline-block;padding:4px 12px;border-radius:12px;font-size:12px;font-weight:700}
            .badge-primary{background:#3498db;color:#fff}
        </style></head><body>
        <div class="container">
            <div class="header"><h1>Recordatorio de Cita</h1></div>
            <div class="body">
                <p>Hola <strong>%s</strong>,</p>
                <p>Te recordamos que tienes una cita programada:</p>
                <table class="info-table">
                    <tr><td>Mascota</td><td><strong>%s</strong> (%s)</td></tr>
                    <tr><td>Fecha</td><td><strong>%s</strong></td></tr>
                    <tr><td>Motivo</td><td>%s</td></tr>
                    <tr><td>Veterinario</td><td>%s %s</td></tr>
                </table>
                <p style="margin-top:16px;color:#777">Por favor llega 10 minutos antes de tu cita. Si necesitas cancelar, contáctanos con al menos 24 horas de anticipación.</p>
            </div>
            <div class="footer">PetClinic Veterinary Clinic &bull; %s</div>
        </div></body></html>
        """.formatted(
                cl.getNombre(),
                m.getNombre(), m.getEspecie().name(),
                c.getFechaHoraInicio().format(DATETIME_FMT),
                c.getMotivo(),
                v.getNombre(), v.getApellido(),
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
        );

        emailService.sendHtml(to, subject, html);
    }

    public void alertaVacuna(Vacuna v) {
        Mascota m = v.getMascota();
        Cliente cl = m.getCliente();

        String to = cl.getPortalEmail() != null ? cl.getPortalEmail() : cl.getEmail();
        if (to == null || to.isBlank()) return;

        boolean isOverdue = v.getFechaProximaDosis() != null && v.getFechaProximaDosis().isBefore(java.time.LocalDate.now());
        String subject = isOverdue ? "Vacuna Vencida - %s - PetClinic".formatted(v.getNombre())
                : "Próxima Vacuna - %s - PetClinic".formatted(v.getNombre());

        String alertType = isOverdue ? "VENCIDA" : "PRÓXIMA DOSIS";
        String badgeClass = isOverdue ? "badge-danger" : "badge-warning";
        String msg = isOverdue
                ? "<p>La siguiente vacuna de <strong>%s</strong> está <strong>VENCIDA</strong>. Por favor agenda una cita lo antes posible.</p>".formatted(m.getNombre())
                : "<p>La siguiente vacuna de <strong>%s</strong> tiene una dosis próxima. Te sugerimos agendar una cita.</p>".formatted(m.getNombre());

        String html = """
        <!DOCTYPE html>
        <html><head><meta charset="UTF-8"><style>
            body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}
            .container{max-width:600px;margin:auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)}
            .header{background:%%s;color:#fff;padding:20px;text-align:center}
            .header h1{margin:0;font-size:20px}
            .body{padding:24px}
            .info-table{width:100%%%%;border-collapse:collapse}
            .info-table td{padding:8px 0;border-bottom:1px solid #eee}
            .info-table td:first-child{font-weight:700;color:#555;width:140px}
            .footer{background:#f8f9fa;padding:16px;text-align:center;font-size:12px;color:#888}
            .badge{display:inline-block;padding:4px 12px;border-radius:12px;font-size:12px;font-weight:700;color:#fff}
            .badge-danger{background:#e74c3c}
            .badge-warning{background:#f39c12}
        </style></head><body>
        <div class="container">
            <div class="header" style="background:%%s"><h1>Alerta de Vacuna: <span class="badge %s">%s</span></h1></div>
            <div class="body">
                <p>Hola <strong>%s</strong>,</p>
                %s
                <table class="info-table">
                    <tr><td>Vacuna</td><td><strong>%s</strong></td></tr>
                    <tr><td>Mascota</td><td><strong>%s</strong> (%s)</td></tr>
                    <tr><td>Última Dosis</td><td><strong>%s</strong></td></tr>
                    <tr><td>Próxima Dosis</td><td><strong>%s</strong></td></tr>
                    <tr><td>Fabricante</td><td>%s</td></tr>
                    <tr><td>Lote</td><td>%s</td></tr>
                </table>
            </div>
            <div class="footer">PetClinic Veterinary Clinic &bull; Agenda al teléfono de la clínica</div>
        </div></body></html>
        """.formatted(
                isOverdue ? "#e74c3c" : "#f39c12",
                isOverdue ? "#e74c3c" : "#f39c12",
                badgeClass, alertType,
                cl.getNombre(),
                msg,
                v.getNombre(),
                m.getNombre(), m.getEspecie().name(),
                v.getFechaAplicacion() != null ? v.getFechaAplicacion().format(DATE_FMT) : "-",
                v.getFechaProximaDosis() != null ? v.getFechaProximaDosis().format(DATE_FMT) : "-",
                v.getFabricante() != null ? v.getFabricante() : "-",
                v.getNumeroLote() != null ? v.getNumeroLote() : "-"
        );

        emailService.sendHtml(to, subject, html);
    }

    public void notificarFactura(Long facturaId) {
        Factura f = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        notificarFactura(f);
    }

    public void notificarFactura(Factura f) {
        Cliente cl = f.getCliente();

        String to = cl.getPortalEmail() != null ? cl.getPortalEmail() : cl.getEmail();
        if (to == null || to.isBlank()) return;

        String subject = "Factura %s - PetClinic Veterinary Clinic".formatted(f.getNumeroFactura());
        String html = """
        <!DOCTYPE html>
        <html><head><meta charset="UTF-8"><style>
            body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}
            .container{max-width:600px;margin:auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)}
            .header{background:#27ae60;color:#fff;padding:20px;text-align:center}
            .header h1{margin:0;font-size:20px}
            .body{padding:24px}
            .info-table{width:100%%;border-collapse:collapse}
            .info-table td{padding:8px 0;border-bottom:1px solid #eee}
            .info-table td:first-child{font-weight:700;color:#555;width:140px}
            .total-row{font-size:18px;font-weight:700;color:#27ae60}
            .footer{background:#f8f9fa;padding:16px;text-align:center;font-size:12px;color:#888}
            .btn{display:inline-block;padding:10px 24px;background:#27ae60;color:#fff;text-decoration:none;border-radius:6px;margin-top:12px}
        </style></head><body>
        <div class="container">
            <div class="header"><h1>Factura %s</h1></div>
            <div class="body">
                <p>Hola <strong>%s</strong>,</p>
                <p>Se ha generado una nueva factura:</p>
                <table class="info-table">
                    <tr><td>Número</td><td><strong>%s</strong></td></tr>
                    <tr><td>Fecha</td><td><strong>%s</strong></td></tr>
                    <tr><td>Total</td><td class="total-row">$%.2f MXN</td></tr>
                    <tr><td>Estado</td><td><strong>%s</strong></td></tr>
                </table>
                <a href="%s" class="btn">Ver Factura</a>
            </div>
            <div class="footer">PetClinic Veterinary Clinic &bull; Gracias por tu preferencia</div>
        </div></body></html>
        """.formatted(
                f.getNumeroFactura(),
                cl.getNombre(),
                f.getNumeroFactura(),
                f.getFechaEmision().format(DATETIME_FMT),
                f.getTotal(),
                f.getEstado().name(),
                "http://localhost:8090"
        );

        emailService.sendHtml(to, subject, html);
    }
}
