package com.veterinary;

import com.veterinary.dto.LoginRequest;
import com.veterinary.dto.LoginResponse;
import com.veterinary.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NewFeaturesIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static String adminToken;
    private static String vetToken;
    private static Long testFacturaId;

    @Test
    @Order(1)
    void testLoginAdmin() {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@petclinic.com");
        req.setPassword("Admin123");
        req.setTipo("ADMIN");
        ResponseEntity<LoginResponse> res = restTemplate.postForEntity("/api/auth/login", req, LoginResponse.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody().getToken());
        adminToken = res.getBody().getToken();
    }

    @Test
    @Order(2)
    void testLoginVet() {
        LoginRequest req = new LoginRequest();
        req.setEmail("vet@petclinic.com");
        req.setPassword("Vet123");
        req.setTipo("VETERINARIO");
        ResponseEntity<LoginResponse> res = restTemplate.postForEntity("/api/auth/login", req, LoginResponse.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody().getToken());
        vetToken = res.getBody().getToken();
    }

    // ========== PUBLIC BOOKING TESTS ==========

    @Test
    @Order(10)
    void testPublicListVeterinarios() {
        ResponseEntity<Map[]> res = restTemplate.getForEntity("/api/public/veterinarios", Map[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody().length > 0);
        assertNotNull(res.getBody()[0].get("id"));
        assertNotNull(res.getBody()[0].get("nombre"));
    }

    @Test
    @Order(11)
    void testPublicSlots() {
        ResponseEntity<Map[]> res = restTemplate.getForEntity(
                "/api/public/slots?vetId=1&date=" + LocalDate.now().plusDays(1),
                Map[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody().length > 0);
        assertNotNull(res.getBody()[0].get("hora"));
        assertNotNull(res.getBody()[0].get("disponible"));
    }

    @Test
    @Order(12)
    void testPublicSlotsInvalidVet() {
        ResponseEntity<Map> res = restTemplate.getForEntity(
                "/api/public/slots?vetId=9999&date=" + LocalDate.now().plusDays(1),
                Map.class);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertNotNull(res.getBody().get("error"));
    }

    @Test
    @Order(13)
    void testPublicCreateCita() {
        String futureDate = LocalDate.now().plusDays(14).toString();
        Map<String, String> body = Map.of(
                "veterinarioId", "1",
                "mascotaId", "1",
                "clienteEmail", "ana@email.com",
                "fecha", futureDate,
                "hora", "15:00",
                "motivo", "Cita desde test público"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                "/api/public/citas", HttpMethod.POST, entity, Map.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody().get("cita"));
    }

    @Test
    @Order(14)
    void testPublicCreateCitaWrongEmail() {
        Map<String, String> body = Map.of(
                "veterinarioId", "1",
                "mascotaId", "1",
                "clienteEmail", "wrong@email.com",
                "fecha", LocalDate.now().plusDays(8).toString(),
                "hora", "10:30",
                "motivo", "Cita email incorrecto"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                "/api/public/citas", HttpMethod.POST, entity, Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody().get("error"));
    }

    // ========== ONLINE PAYMENT TESTS ==========

    @Test
    @Order(19)
    void testCreateFacturaForPayment() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
        {
            "clienteId": 1,
            "detalles": [
                {"tipoItem": "SERVICIO", "servicioId": 1, "descripcionLinea": "Consulta", "cantidad": 1, "precioUnitario": 500.0}
            ]
        }
        """;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> res = restTemplate.exchange(
                "/api/medico/facturas", HttpMethod.POST, entity, Map.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        testFacturaId = ((Number) res.getBody().get("id")).longValue();
        assertNotNull(testFacturaId);
    }

    @Test
    @Order(20)
    void testCreatePaymentIntentUnauthenticated() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(Map.of("facturaId", testFacturaId != null ? testFacturaId : 1L), headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                "/api/pagos/stripe/create-payment-intent", HttpMethod.POST, entity, Map.class);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }

    @Test
    @Order(21)
    void testCreatePaymentIntent() {
        assertNotNull(testFacturaId, "testFacturaId debe estar inicializada");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(Map.of("facturaId", testFacturaId), headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                "/api/pagos/stripe/create-payment-intent", HttpMethod.POST, entity, Map.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        Map body = res.getBody();
        assertNotNull(body.get("mode"));
        assertNotNull(body.get("facturaId"));
        assertNotNull(body.get("numeroFactura"));
        assertNotNull(body.get("total"));
    }

    @Test
    @Order(22)
    void testCreatePaymentIntentInvalidFactura() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(Map.of("facturaId", 9999L), headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                "/api/pagos/stripe/create-payment-intent", HttpMethod.POST, entity, Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    // ========== VACCINE SCHEDULER TESTS ==========

    @Test
    @Order(30)
    void testVacunasForMascota() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(vetToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map[]> res = restTemplate.exchange(
                "/api/medico/vacunas/mascota/1", HttpMethod.GET, entity, Map[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    // ========== NOTIFICACION ENDPOINTS ==========

    @Test
    @Order(40)
    void testCitaCreateTriggersNotification() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String futureDate = LocalDate.now().plusDays(21).toString();
        String body = """
        {
            "mascotaId": 1,
            "fechaHoraInicio": "%sT10:00:00",
            "fechaHoraFin": "%sT10:30:00",
            "motivo": "Test notificación cita"
        }
        """.formatted(futureDate, futureDate);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> res = restTemplate.exchange(
                "/api/medico/citas", HttpMethod.POST, entity, Map.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    // ========== BEAN EXISTENCE ==========

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @Order(50)
    void testAllNewServiceBeansExist() {
        assertNotNull(applicationContext.getBean(EmailService.class));
        assertNotNull(applicationContext.getBean(NotificacionService.class));
        assertNotNull(applicationContext.getBean(PublicBookingService.class));
        assertNotNull(applicationContext.getBean(PagoOnlineService.class));
        assertNotNull(applicationContext.getBean(com.veterinary.scheduler.VacunaScheduler.class));
    }

    // ========== SECURITY - PUBLIC ENDPOINTS DON'T NEED AUTH ==========

    @Test
    @Order(60)
    void testPublicEndpointsNoAuthRequired() {
        ResponseEntity<Map[]> res = restTemplate.getForEntity("/api/public/veterinarios", Map[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    @Order(61)
    void testPaymentEndpointRequiresAuth() {
        ResponseEntity<Map> res = restTemplate.postForEntity(
                "/api/pagos/stripe/create-payment-intent", 
                Map.of("facturaId", 1L), 
                Map.class);
        assertTrue(res.getStatusCode() == HttpStatus.FORBIDDEN 
                || res.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }
}
