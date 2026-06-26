package com.veterinary.scheduler;

import com.veterinary.domain.Vacuna;
import com.veterinary.repository.VacunaRepository;
import com.veterinary.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class VacunaScheduler {

    private static final Logger log = LoggerFactory.getLogger(VacunaScheduler.class);

    private final VacunaRepository vacunaRepository;
    private final EmailService emailService;

    public VacunaScheduler(VacunaRepository vacunaRepository, EmailService emailService) {
        this.vacunaRepository = vacunaRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void verificarVacunasProximas() {
        log.info("Verificando vacunas próximas a vencer...");
        LocalDate hoy = LocalDate.now();
        LocalDate dentroDe7Dias = hoy.plusDays(7);

        List<Vacuna> proximas = vacunaRepository.findByFechaProximaDosisBetween(hoy, dentroDe7Dias);
        for (Vacuna v : proximas) {
            try {
                enviarAlerta(v);
            } catch (Exception e) {
                log.error("Error al enviar alerta de vacuna ID {}: {}", v.getId(), e.getMessage());
            }
        }
        log.info("Alertas de vacunas enviadas: {}", proximas.size());
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void verificarVacunasVencidas() {
        log.info("Verificando vacunas vencidas...");
        LocalDate ayer = LocalDate.now().minusDays(1);

        List<Vacuna> vencidas = vacunaRepository.findByFechaProximaDosisBetween(
                LocalDate.of(1900, 1, 1), ayer
        );

        List<Vacuna> sinProximaDosis = vacunaRepository.findAll().stream()
                .filter(v -> v.getFechaProximaDosis() != null && v.getFechaProximaDosis().isBefore(LocalDate.now()))
                .toList();

        int count = 0;
        for (Vacuna v : sinProximaDosis) {
            if (v.getFechaProximaDosis() != null && v.getFechaProximaDosis().isBefore(LocalDate.now())
                    && v.getFechaProximaDosis().isAfter(LocalDate.now().minusMonths(1))) {
                try {
                    enviarAlerta(v);
                    count++;
                } catch (Exception e) {
                    log.error("Error al enviar alerta de vacuna vencida ID {}: {}", v.getId(), e.getMessage());
                }
            }
        }
        log.info("Alertas de vacunas vencidas enviadas: {}", count);
    }

    private void enviarAlerta(Vacuna v) {
        String to = v.getMascota().getCliente().getEmail();
        if (to == null || to.isBlank()) return;

        boolean vencida = v.getFechaProximaDosis() != null && v.getFechaProximaDosis().isBefore(LocalDate.now());
        String tipo = vencida ? "VENCIDA" : "PRÓXIMA DOSIS";

        String html = """
        <!DOCTYPE html>
        <html><head><meta charset="UTF-8"><style>
            body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}
            .container{max-width:600px;margin:auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)}
            .header{%s;color:#fff;padding:20px;text-align:center}
            .header h1{margin:0;font-size:20px}
            .body{padding:24px}
            .info-table{width:100%%;border-collapse:collapse}
            .info-table td{padding:8px 0;border-bottom:1px solid #eee}
            .info-table td:first-child{font-weight:700;color:#555;width:140px}
            .footer{background:#f8f9fa;padding:16px;text-align:center;font-size:12px;color:#888}
        </style></head><body>
        <div class="container">
            <div class="header" style="background:%s"><h1>Alerta de Vacuna: %s</h1></div>
            <div class="body">
                <p>Hola <strong>%s</strong>,</p>
                <p>%s</p>
                <table class="info-table">
                    <tr><td>Vacuna</td><td><strong>%s</strong></td></tr>
                    <tr><td>Mascota</td><td><strong>%s</strong> (%s)</td></tr>
                    <tr><td>Última Dosis</td><td>%s</td></tr>
                    <tr><td>Próxima Dosis</td><td><strong>%s</strong></td></tr>
                    <tr><td>Fabricante</td><td>%s</td></tr>
                </table>
            </div>
            <div class="footer">PetClinic Veterinary Clinic &bull; Sistema automático de notificaciones</div>
        </div></body></html>
        """.formatted(
                vencida ? "background:#e74c3c" : "background:#f39c12",
                vencida ? "#e74c3c" : "#f39c12",
                tipo,
                v.getMascota().getCliente().getNombre(),
                vencida
                        ? "La vacuna <strong>%s</strong> de <strong>%s</strong> está VENCIDA. Agenda una cita pronto."
                            .formatted(v.getNombre(), v.getMascota().getNombre())
                        : "La vacuna <strong>%s</strong> de <strong>%s</strong> tiene una dosis próxima."
                            .formatted(v.getNombre(), v.getMascota().getNombre()),
                v.getNombre(),
                v.getMascota().getNombre(), v.getMascota().getEspecie().name(),
                v.getFechaAplicacion() != null ? v.getFechaAplicacion().toString() : "-",
                v.getFechaProximaDosis() != null ? v.getFechaProximaDosis().toString() : "-",
                v.getFabricante() != null ? v.getFabricante() : "-"
        );

        emailService.sendHtml(to, "Alerta de Vacuna - PetClinic", html);
    }
}
