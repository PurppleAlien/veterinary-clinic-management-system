package com.veterinary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veterinary.dto.CitaDto;
import com.veterinary.dto.LoginRequest;
import com.veterinary.dto.LoginResponse;
import com.veterinary.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VeterinarySystemIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static String vetToken;
    private static Long clienteId;
    private static Long mascotaId;
    private static Long citaId;
    private static Long facturaId;

    @Test
    @Order(1)
    void testLoginAdmin() {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@petclinic.com");
        req.setPassword("Admin123");
        req.setTipo("ADMIN");

        ResponseEntity<LoginResponse> res = restTemplate.postForEntity(
                "/api/auth/login", req, LoginResponse.class);
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

        ResponseEntity<LoginResponse> res = restTemplate.postForEntity(
                "/api/auth/login", req, LoginResponse.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody().getToken());
        vetToken = res.getBody().getToken();
    }

    @Test
    @Order(3)
    void testDashboard() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                "/api/medico/dashboard", HttpMethod.GET, entity, Map.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody().containsKey("totalClientes"));
    }

    @Test
    @Order(4)
    void testGetClientes() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map[]> res = restTemplate.exchange(
                "/api/medico/clientes", HttpMethod.GET, entity, Map[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody().length > 0);
        clienteId = ((Number) res.getBody()[0].get("id")).longValue();
    }

    @Test
    @Order(5)
    void testGetMascotas() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map[]> res = restTemplate.exchange(
                "/api/medico/mascotas", HttpMethod.GET, entity, Map[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody().length > 0);
        mascotaId = ((Number) res.getBody()[0].get("id")).longValue();
    }

    @Test
    @Order(6)
    void testCreateCita() {
        CitaDto dto = new CitaDto();
        dto.setMascotaId(mascotaId);
        dto.setFechaHoraInicio(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0));
        dto.setFechaHoraFin(LocalDateTime.now().plusDays(7).withHour(10).withMinute(30));
        dto.setMotivo("Cita de prueba");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<CitaDto> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<CitaDto> res = restTemplate.exchange(
                "/api/medico/citas", HttpMethod.POST, entity, CitaDto.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody().getId());
        citaId = res.getBody().getId();
    }

    @Test
    @Order(7)
    void testGetServicios() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(vetToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map[]> res = restTemplate.exchange(
                "/api/medico/servicios", HttpMethod.GET, entity, Map[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody().length > 0);
    }

    @Test
    @Order(8)
    void testGetMedicamentos() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(vetToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map[]> res = restTemplate.exchange(
                "/api/medico/medicamentos", HttpMethod.GET, entity, Map[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody().length > 0);
    }

    @Test
    @Order(9)
    void testCreateFactura() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
        {
            "clienteId": %d,
            "detalles": [
                {"tipoItem": "SERVICIO", "servicioId": 1, "descripcionLinea": "Consulta General", "cantidad": 1, "precioUnitario": 500},
                {"tipoItem": "MEDICAMENTO", "medicamentoId": 1, "descripcionLinea": "Amoxicilina", "cantidad": 2, "precioUnitario": 15}
            ]
        }
        """.formatted(clienteId);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> res = restTemplate.exchange(
                "/api/medico/facturas", HttpMethod.POST, entity, Map.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        facturaId = ((Number) res.getBody().get("id")).longValue();
    }

    @Test
    @Order(10)
    void testGetFacturaPdf() {
        if (facturaId == null) return;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> res = restTemplate.exchange(
                "/api/medico/facturas/" + facturaId + "/pdf", HttpMethod.GET, entity, byte[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody().length > 0);
    }

    @Test
    @Order(11)
    void testHistorialMedico() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(vetToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map[]> res = restTemplate.exchange(
                "/api/medico/historial/mascota/" + mascotaId, HttpMethod.GET, entity, Map[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    @Order(12)
    void testHospitalizaciones() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map[]> res = restTemplate.exchange(
                "/api/medico/hospitalizaciones", HttpMethod.GET, entity, Map[].class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    @Order(13)
    void testPortalAccessDenied() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(vetToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> res = restTemplate.exchange(
                "/api/portal/perfil", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }

    @Test
    @Order(14)
    void testLoginCliente() {
        LoginRequest req = new LoginRequest();
        req.setEmail("ana@email.com");
        req.setPassword("Ana123");
        req.setTipo("CLIENTE");

        ResponseEntity<LoginResponse> res = restTemplate.postForEntity(
                "/api/auth/login", req, LoginResponse.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody().getToken());
    }

    @Test
    @Order(15)
    void testDeleteCita() {
        if (citaId == null) return;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                "/api/medico/citas/" + citaId, HttpMethod.DELETE, entity, Map.class);
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }
}
