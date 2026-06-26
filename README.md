# Veterinary Clinic Management System 🐾

Sistema de Gestión de Clínica Veterinaria desarrollado con **Java 21**, **Spring Boot 3.2.3**, **PostgreSQL** y **JWT**.

Proyecto inspirado en dental-clinic-management-system, adaptado con lógica de negocio veterinaria.

---

## 📋 Tabla de Contenidos

1. [Tecnologías](#-tecnologías)
2. [Arquitectura](#-arquitectura)
3. [Patrones de Diseño](#-patrones-de-diseño)
4. [Estructura del Proyecto](#-estructura-del-proyecto)
5. [Base de Datos](#-base-de-datos)
6. [API Endpoints](#-api-endpoints)
7. [Frontend](#-frontend)
8. [Requisitos](#-requisitos)
9. [Instalación y Ejecución](#-instalación-y-ejecución)
10. [Credenciales de Prueba](#-credenciales-de-prueba)
11. [Funcionalidades](#-funcionalidades)

---

## 🛠 Tecnologías

| Capa | Tecnología |
|------|-----------|
| **Lenguaje** | Java 21 |
| **Framework** | Spring Boot 3.2.3 |
| **Base de Datos** | PostgreSQL v14+ |
| **ORM** | Spring Data JPA / Hibernate |
| **Seguridad** | Spring Security + JWT (jjwt 0.12.3) |
| **WebSocket** | Spring WebSocket + STOMP + SockJS |
| **PDF** | iText 7 Core (8.0.3) |
| **Excel** | Apache POI (5.2.5) |
| **Frontend** | Bootstrap 5 + FullCalendar 6 + Chart.js 4 |
| **Build** | Maven |
| **Testing** | JUnit 5 + SpringBootTest |

---

## 🏗 Arquitectura

**Layered Architecture** (Arquitectura en Capas):

```
Cliente (SPA) → Controller (REST API) → Service (Lógica de negocio) → Repository (DAO) → DB (PostgreSQL)
```

### Flujo de una petición

```
HTTP Request
    ↓
JwtAuthFilter (valida token JWT)
    ↓
SecurityConfig (verifica rol: ADMIN / VETERINARIO / RECEPCIONISTA / CLIENTE)
    ↓
Controller (@RestController)
    ↓
Service (@Service, @Transactional)
    ↓
Repository (Spring Data JPA)
    ↓
Base de Datos
```

### Roles de usuario

| Rol | Acceso |
|-----|--------|
| `ADMIN` | Todo el sistema, gestión de veterinarios, auditoría |
| `VETERINARIO` | CRUD clínico: citas, historial, facturas, planes |
| `RECEPCIONISTA` | Lectura de datos, creación de clientes/citas |
| `CLIENTE` | Portal propio: ver citas, facturas, historial de mascotas |

---

## 🧩 Patrones de Diseño

| Patrón | Implementación |
|--------|---------------|
| **Singleton** | Spring Beans (`@Service`, `@Repository`, `@Controller`) |
| **Dependency Injection** | Constructor Injection en todas las clases |
| **Factory** | Spring Data JPA genera repositorios automáticamente |
| **Chain of Responsibility** | Security Filter Chain (`JwtAuthFilter` → `UsernamePasswordAuthenticationFilter`) |
| **Observer / Pub-Sub** | WebSocket STOMP para actualizaciones de citas en tiempo real |
| **Strategy** | `PasswordEncoder` (BCrypt) intercambiable |
| **Template Method** | JPA Lifecycle Callbacks (`@PrePersist`, `@PreUpdate`) |
| **DTO Pattern** | `CitaDto`, `FacturaDto`, `LoginRequest/Response` separan API de entidades |
| **Global Exception Handler** | `@RestControllerAdvice` con `GlobalExceptionHandler` |
| **CommandLineRunner** | `DataInitializer` para seed data al arrancar |
| **DAO Pattern** | Repositorios encapsulan operaciones de base de datos |
| **Scheduling** | `@Scheduled` en `VacunaScheduler` para alertas automáticas de vacunas |

---

## 📁 Estructura del Proyecto

```
veterinary-clinic-management-system/
│
├── pom.xml                              # Dependencias Maven
├── README.md
├── .gitignore
│
└── src/
    ├── main/
    │   ├── java/com/veterinary/
    │   │   ├── VeterinaryApplication.java      # Entry point (@SpringBootApplication)
    │   │   │
    │   │   ├── config/                         # Configuraciones
    │   │   │   ├── DataInitializer.java        # Seed data (CommandLineRunner)
    │   │   │   ├── GlobalExceptionHandler.java # Manejador global de errores
    │   │   │   ├── WebConfig.java              # CORS y recursos estáticos
    │   │   │   └── WebSocketConfig.java        # STOMP sobre SockJS
    │   │   │
    │   │   ├── controller/                     # REST Controllers (20)
    │   │   │   ├── AuthController.java         # POST /api/auth/login
    │   │   │   ├── DashboardController.java    # Estadísticas del dashboard
    │   │   │   ├── VeterinarioController.java  # CRUD veterinarios (admin)
    │   │   │   ├── ClienteController.java      # CRUD clientes + búsqueda
    │   │   │   ├── MascotaController.java      # CRUD mascotas
    │   │   │   ├── CitaController.java         # CRUD citas + WebSocket
    │   │   │   ├── ServicioController.java     # CRUD servicios
    │   │   │   ├── MedicamentoController.java  # CRUD + ajuste de stock
    │   │   │   ├── FacturaController.java      # CRUD + pagos + PDF
    │   │   │   ├── HistorialMedicoController.java # Notas de consulta
    │   │   │   ├── VacunaController.java       # Registro de vacunas
    │   │   │   ├── HospitalizacionController.java # Hospitalización
    │   │   │   ├── PlanTratamientoController.java # Planes + pasos
    │   │   │   ├── ConsentimientoController.java  # Consentimientos + firma + PDF
    │   │   │   ├── ReporteController.java      # Ingresos + Excel
    │   │   │   ├── PortalClienteController.java   # Portal del cliente
    │   │   │   ├── OdontogramaController.java  # Diagrama dental
    │   │   │   ├── PagoOnlineController.java   # Pagos Stripe
    │   │   │   ├── PublicBookingController.java   # Reserva pública
    │   │   │   └── AuditController.java        # Logs de auditoría
    │   │   │
    │   │   ├── domain/                         # Entidades JPA (20)
    │   │   │   ├── Veterinario.java
    │   │   │   ├── Cliente.java
    │   │   │   ├── Mascota.java
    │   │   │   ├── Cita.java
    │   │   │   ├── Servicio.java
    │   │   │   ├── Medicamento.java
    │   │   │   ├── MovimientoInventario.java
    │   │   │   ├── Factura.java
    │   │   │   ├── DetalleFactura.java
    │   │   │   ├── Pago.java
    │   │   │   ├── HistorialMedico.java
    │   │   │   ├── Vacuna.java
    │   │   │   ├── Hospitalizacion.java
    │   │   │   ├── PlanTratamiento.java
    │   │   │   ├── PasoTratamiento.java
    │   │   │   ├── ConsentimientoInformado.java
    │   │   │   ├── AuditLog.java
    │   │   │   ├── Odontograma.java            # Diagrama dental
    │   │   │   ├── OdontogramaDetalle.java     # Estado por diente
    │   │   │   └── enums/                      # Enumeraciones (10)
    │   │   │       ├── RolUsuario.java         # ADMIN, VETERINARIO, RECEPCIONISTA
    │   │   │       ├── EstadoCita.java         # PROGRAMADA..NO_ASISTIO
    │   │   │       ├── EstadoFactura.java      # PENDIENTE..ANULADA
    │   │   │       ├── MetodoPago.java         # EFECTIVO..CHEQUE
    │   │   │       ├── Especie.java            # CANINO, FELINO..OTRO
    │   │   │       ├── GeneroMascota.java      # MACHO, HEMBRA
    │   │   │       ├── EstadoHospitalizacion.java
    │   │   │       ├── EstadoPlan.java
    │   │   │       ├── EstadoPaso.java
    │   │   │       └── TipoMovimiento.java
    │   │   │
    │   │   ├── dto/                            # Data Transfer Objects (6)
    │   │   │   ├── LoginRequest.java
    │   │   │   ├── LoginResponse.java
    │   │   │   ├── CitaDto.java
    │   │   │   ├── FacturaDto.java             # + DetalleFacturaDto, PagoDto
    │   │   │   ├── MascotaDto.java
    │   │   │   └── ClienteDto.java
    │   │   │
    │   │   ├── repository/                     # Spring Data JPA (19 interfaces)
    │   │   │   ├── VeterinarioRepository.java
    │   │   │   ├── ClienteRepository.java      # + búsqueda texto libre
    │   │   │   ├── MascotaRepository.java
    │   │   │   ├── CitaRepository.java         # + consultas solapamiento
    │   │   │   ├── ServicioRepository.java
    │   │   │   ├── MedicamentoRepository.java  # + stock bajo
    │   │   │   ├── MovimientoInventarioRepository.java
    │   │   │   ├── FacturaRepository.java      # + suma ingresos por fecha
    │   │   │   ├── DetalleFacturaRepository.java
    │   │   │   ├── PagoRepository.java
    │   │   │   ├── HistorialMedicoRepository.java
    │   │   │   ├── VacunaRepository.java
    │   │   │   ├── HospitalizacionRepository.java
    │   │   │   ├── PlanTratamientoRepository.java
    │   │   │   ├── PasoTratamientoRepository.java
    │   │   │   ├── ConsentimientoRepository.java
    │   │   │   ├── AuditLogRepository.java
    │   │   │   ├── OdontogramaRepository.java
    │   │   │   └── OdontogramaDetalleRepository.java
    │   │   │
    │   │   ├── security/                       # Seguridad JWT (4)
    │   │   │   ├── JwtTokenProvider.java       # Generar/validar tokens
    │   │   │   ├── JwtAuthFilter.java          # Filtro por petición
    │   │   │   ├── SecurityConfig.java         # Cadena de filtros
    │   │   │   └── CustomUserDetailsService.java
    │   │   │
    │   │   ├── scheduler/                      # Tareas programadas
    │   │   │   └── VacunaScheduler.java        # Alertas de vacunación
    │   │   │
    │   │   └── service/                        # Servicios (21)
    │   │       ├── CitaService.java
    │   │       ├── FacturaService.java
    │   │       ├── PdfService.java
    │   │       ├── AuditService.java
    │   │       ├── MascotaService.java
    │   │       ├── ClienteService.java
    │   │       ├── VeterinarioService.java
    │   │       ├── ServicioService.java
    │   │       ├── MedicamentoService.java
    │   │       ├── HistorialMedicoService.java
    │   │       ├── VacunaService.java
    │   │       ├── HospitalizacionService.java
    │   │       ├── PlanTratamientoService.java
    │   │       ├── ConsentimientoService.java
    │   │       ├── DashboardService.java
    │   │       ├── ReporteService.java
    │   │       ├── EmailService.java           # Notificaciones email
    │   │       ├── NotificacionService.java    # Alertas multicanal
    │   │       ├── PagoOnlineService.java      # Stripe
    │   │       ├── PublicBookingService.java   # Reserva pública
    │   │       └── OdontogramaService.java     # Diagrama dental
    │   │
    │   └── resources/
    │       ├── application.properties          # Configuración de la app
    │       └── static/                         # Frontend SPA
    │           ├── index.html                  # Página principal
    │           ├── css/app.css                 # Estilos (8 temas de color)
    │           └── js/
    │               ├── api.js                  # Cliente HTTP con JWT
    │               ├── app.js                  # Lógica del SPA
    │               └── vet-extras.js           # Selector de temas
    │
    └── test/java/com/veterinary/
        ├── VeterinarySystemIntegrationTest.java  # 15 tests de integración
        └── NewFeaturesIntegrationTest.java       # 15 tests adicionales
```

---

## 🗄 Base de Datos

### Esquema (19 tablas + 1 auditoría)

JPA genera automáticamente las tablas con `spring.jpa.hibernate.ddl-auto=update`.

| Tabla | Descripción | Columnas clave |
|-------|------------|----------------|
| `veterinarios` | Usuarios del sistema (admin, vets, recep) | nombre, email, password_hash, rol, horarios |
| `clientes` | Dueños de mascotas | nombre, email, teléfono, portal_email, portal_password_hash |
| `mascotas` | Pacientes animales | nombre, especie, raza, género, peso, cliente_id (FK) |
| `citas` | Agendamiento de consultas | mascota_id, veterinario_id, fecha_hora_inicio/fin, estado |
| `servicios` | Catálogo de servicios veterinarios | nombre, precio_base, código_interno |
| `medicamentos` | Inventario de farmacia | nombre, unidad, stock_actual, stock_minimo, precio_unitario |
| `movimientos_inventario` | Trazabilidad de stock | medicamento_id, tipo (ENTRADA/SALIDA/AJUSTE), cantidad |
| `facturas` | Facturación | cliente_id, veterinario_id, subtotal, total, estado |
| `detalle_factura` | Líneas de factura | tipo_item (SERVICIO/MEDICAMENTO), cantidad, precio_unitario |
| `pagos` | Pagos registrados | factura_id, monto, método, referencia |
| `historial_medico` | Notas de consulta | mascota_id, veterinario_id, motivo, diagnóstico, medicación |
| `vacunas` | Historial de vacunación | mascota_id, nombre, fecha_aplicación, fecha_próxima_dosis |
| `hospitalizaciones` | Hospitalización de mascotas | mascota_id, check_in, check_out, jaula, estado |
| `planes_tratamiento` | Planes de tratamiento | mascota_id, título, costo_estimado, estado |
| `pasos_tratamiento` | Pasos de un plan | plan_id, descripción, servicio_id, orden, estado |
| `consentimientos_informados` | Consentimientos para procedimientos | cliente_id, tipo_procedimiento, firmado, fecha_firma |
| `audit_log` | Auditoría de acciones | acción, entidad, veterinario_id, fecha |
| `odontogramas` | Diagrama dental de mascota | mascota_id, fecha, notas |
| `odontograma_detalles` | Estado por diente | odontograma_id, numero_diente, estado (SANO..ENDODONCIA) |

### Relaciones principales

```
Cliente (1) ──→ Mascota (N) ──→ Cita (N) ──→ Veterinario (1)
                                    ↓
                              HistorialMedico
                              Vacuna
                              Factura ──→ DetalleFactura ──→ Servicio / Medicamento
                                         ──→ Pago
                              PlanTratamiento ──→ PasoTratamiento
```

---

## 🌐 API Endpoints

### Autenticación

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/auth/login` | Iniciar sesión (personal o cliente) |

**Body:**
```json
{ "email": "admin@petclinic.com", "password": "Admin123", "tipo": "ADMIN" }
```

**Response:**
```json
{ "token": "eyJ...", "email": "...", "nombre": "...", "rol": "ADMIN", "userId": 1 }
```

---

### Dashboard

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/dashboard` | Estadísticas del dashboard |

---

### Veterinarios (solo ADMIN)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/veterinarios` | Listar todos |
| `GET` | `/api/medico/veterinarios/{id}` | Obtener uno |
| `POST` | `/api/medico/veterinarios` | Crear |
| `PUT` | `/api/medico/veterinarios/{id}` | Actualizar |
| `DELETE` | `/api/medico/veterinarios/{id}` | Desactivar |

---

### Clientes

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/clientes?q=texto` | Listar con búsqueda opcional |
| `GET` | `/api/medico/clientes/{id}` | Obtener con mascotas |
| `POST` | `/api/medico/clientes` | Crear |
| `PUT` | `/api/medico/clientes/{id}` | Actualizar |
| `DELETE` | `/api/medico/clientes/{id}` | Eliminar |

---

### Mascotas

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/mascotas` | Listar todas |
| `GET` | `/api/medico/mascotas/{id}` | Obtener detalle |
| `GET` | `/api/medico/mascotas/cliente/{clienteId}` | Por cliente |
| `POST` | `/api/medico/mascotas` | Crear |
| `PUT` | `/api/medico/mascotas/{id}` | Actualizar |
| `DELETE` | `/api/medico/mascotas/{id}` | Eliminar |

---

### Citas

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/citas` | Listar todas |
| `GET` | `/api/medico/citas/{id}` | Obtener una |
| `GET` | `/api/medico/citas/veterinario/{id}` | Por veterinario |
| `GET` | `/api/medico/citas/cliente/{id}` | Por cliente |
| `GET` | `/api/medico/citas/mascota/{id}` | Por mascota |
| `POST` | `/api/medico/citas` | Crear (broadcast WebSocket) |
| `PUT` | `/api/medico/citas/{id}` | Actualizar |
| `DELETE` | `/api/medico/citas/{id}` | Eliminar |

**WebSocket:** Los cambios se transmiten en tiempo real por STOMP en `/topic/citas`.

---

### Servicios

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/servicios` | Listar activos |
| `POST` | `/api/medico/servicios` | Crear |
| `PUT` | `/api/medico/servicios/{id}` | Actualizar |
| `DELETE` | `/api/medico/servicios/{id}` | Activar/Desactivar |

---

### Medicamentos

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/medicamentos` | Listar activos |
| `GET` | `/api/medico/medicamentos/stock-bajo` | Alertas de stock |
| `POST` | `/api/medico/medicamentos` | Crear |
| `PUT` | `/api/medico/medicamentos/{id}` | Actualizar |
| `POST` | `/api/medico/medicamentos/{id}/ajustar-stock` | Ajustar stock (con movimiento) |
| `DELETE` | `/api/medico/medicamentos/{id}` | Activar/Desactivar |

---

### Facturación

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/facturas` | Listar todas |
| `GET` | `/api/medico/facturas/{id}` | Obtener con detalles y pagos |
| `GET` | `/api/medico/facturas/cliente/{id}` | Por cliente |
| `POST` | `/api/medico/facturas` | Crear (descuenta stock automático) |
| `POST` | `/api/medico/facturas/{id}/pago` | Registrar pago |
| `GET` | `/api/medico/facturas/{id}/pdf` | Descargar PDF |
| `DELETE` | `/api/medico/facturas/{id}` | Eliminar |

---

### Historial Médico

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/historial/mascota/{id}` | Por mascota |
| `GET` | `/api/medico/historial/cita/{id}` | Por cita |
| `POST` | `/api/medico/historial` | Crear nota |
| `PUT` | `/api/medico/historial/{id}` | Actualizar |
| `DELETE` | `/api/medico/historial/{id}` | Eliminar |

---

### Vacunas

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/vacunas/mascota/{id}` | Por mascota |
| `POST` | `/api/medico/vacunas` | Registrar |
| `DELETE` | `/api/medico/vacunas/{id}` | Eliminar |

---

### Hospitalizaciones

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/hospitalizaciones` | Listar activas |
| `GET` | `/api/medico/hospitalizaciones/{id}` | Obtener |
| `POST` | `/api/medico/hospitalizaciones` | Ingresar mascota |
| `PUT` | `/api/medico/hospitalizaciones/{id}/alta` | Dar de alta |
| `DELETE` | `/api/medico/hospitalizaciones/{id}` | Eliminar |

---

### Planes de Tratamiento

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/planes/mascota/{id}` | Por mascota |
| `GET` | `/api/medico/planes/{id}` | Obtener con pasos |
| `POST` | `/api/medico/planes` | Crear |
| `PUT` | `/api/medico/planes/{id}` | Actualizar |
| `POST` | `/api/medico/planes/{planId}/pasos` | Agregar paso |
| `PUT` | `/api/medico/planes/pasos/{id}` | Actualizar paso |
| `PATCH` | `/api/medico/planes/pasos/{id}/estado` | Cambiar estado del paso |
| `DELETE` | `/api/medico/planes/{id}` | Eliminar plan |
| `DELETE` | `/api/medico/planes/pasos/{id}` | Eliminar paso |

---

### Consentimientos Informados

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/consentimientos/cliente/{id}` | Por cliente |
| `GET` | `/api/medico/consentimientos/{id}` | Obtener |
| `POST` | `/api/medico/consentimientos` | Crear |
| `PUT` | `/api/medico/consentimientos/{id}` | Actualizar |
| `PATCH` | `/api/medico/consentimientos/{id}/firmar` | Firmar |
| `GET` | `/api/medico/consentimientos/{id}/pdf` | Descargar PDF |
| `DELETE` | `/api/medico/consentimientos/{id}` | Eliminar |

---

### Reportes

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/reportes/ingresos?desde=YYYY-MM-DD&hasta=YYYY-MM-DD` | Ingresos por período |
| `GET` | `/api/medico/reportes/ingresos/excel` | Descargar Excel |

---

### Portal Cliente (rol CLIENTE)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/portal/perfil` | Perfil del cliente |
| `GET` | `/api/portal/citas` | Citas del cliente |
| `GET` | `/api/portal/facturas` | Facturas del cliente |
| `GET` | `/api/portal/historial/{mascotaId}` | Historial de mascota |
| `GET` | `/api/portal/facturas/{id}/pdf` | Descargar PDF factura |

---
### Auditoría

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/audit-logs` | Listar logs de auditoría |

---
### Odontograma

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/medico/odontogramas/mascota/{mascotaId}` | Obtener odontograma por mascota |
| `POST` | `/api/medico/odontogramas` | Crear odontograma |
| `PUT` | `/api/medico/odontogramas/{id}` | Actualizar odontograma |
| `DELETE` | `/api/medico/odontogramas/{id}` | Eliminar odontograma |

---
### Reserva Pública (sin autenticación)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/public/veterinarios` | Listar veterinarios disponibles |
| `GET` | `/api/public/slots?veterinarioId={id}&fecha=YYYY-MM-DD` | Ver slots disponibles |
| `POST` | `/api/public/citas` | Crear cita como visitante |

---
### Pagos Online (Stripe)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/pagos/stripe/create-payment-intent` | Crear intención de pago Stripe |
| `POST` | `/api/pagos/stripe/webhook` | Webhook de confirmación Stripe |

---

## 🎨 Frontend

**SPA (Single Page Application)** con las siguientes secciones:

| Sección | Descripción |
|---------|-------------|
| **Login** | Pantalla de inicio de sesión con selección de tipo (Personal/Cliente) + pestaña pública "Agendar Cita" |
| **Odontograma** | Diagrama dental interactivo con estado por diente (SANO, TÁRTARO, CARIES, etc.) |
| **Dashboard** | 7 tarjetas con estadísticas: clientes, mascotas, vets, citas hoy, pendientes, hospitalizados, stock bajo |
| **Agenda** | FullCalendar con vista mensual/semanal/diaria, drag & drop, colores por estado |
| **Clientes** | Tabla CRUD con búsqueda, vista detalle con mascotas |
| **Mascotas** | Tabla CRUD con detalle (citas, historial, vacunas, planes) |
| **Facturación** | Tabla de facturas + modal de creación con items dinámicos (servicios/medicamentos) + pagos + PDF |
| **Reportes** | Chart.js de ingresos + descarga Excel |
| **Historial Médico** | Notas de consulta por mascota |
| **Vacunas** | Registro de vacunación |
| **Hospitalizaciones** | Ingreso/alta de mascotas |
| **Planes Tratamiento** | Planes con pasos y estados |
| **Consentimientos** | Creación, visualización, firma y PDF |
| **Servicios** | Catálogo CRUD |
| **Medicamentos** | Inventario CRUD + ajuste de stock + alertas |
| **Admin** | Gestión de veterinarios (solo ADMIN) |
| **Auditoría** | Log de acciones |
| **Configuración** | Perfil y cambio de contraseña |
| **Temas** | 8 temas de color: Light, Dark, Terracota, Blue Pro, Pink, Purple, Olive, Sunset |

### Tecnologías Frontend

- **Bootstrap 5.3** + Bootstrap Icons
- **FullCalendar 6.1** para la agenda de citas
- **Chart.js 4.4** para gráficos de reportes
- **Vanilla JS** sin frameworks (arquitectura simple de secciones)

---

## ⚙️ Requisitos

- **Java 21** o superior
- **PostgreSQL 14** o superior
- **Maven 3.8+**
- Navegador web moderno (Chrome, Firefox, Edge)

---

## 🚀 Instalación y Ejecución

### 1. Clonar el proyecto

```bash
cd /ruta/del/proyecto
```

### 2. Configurar la base de datos PostgreSQL

**Opción A - Usando psql:**
```bash
sudo -u postgres psql -c "CREATE DATABASE veterinary_db;"
sudo -u postgres psql -c "CREATE USER veterinary_user WITH PASSWORD 'veterinary_pass';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE veterinary_db TO veterinary_user;"
```

**Opción B - Usando pgAdmin:**
1. Crear base de datos `veterinary_db`
2. Crear usuario `veterinary_user` con contraseña `veterinary_pass`
3. Asignar todos los privilegios

### 3. (Opcional) Cambiar configuración de BD

Editar `src/main/resources/application.properties` si tu PostgreSQL usa otro puerto, usuario o contraseña:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/veterinary_db
spring.datasource.username=veterinary_user
spring.datasource.password=veterinary_pass
```

### 4. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La aplicación tardará unos segundos en arrancar. Verás el log:
```
INFO  --- Started VeterinaryApplication in X seconds
Datos iniciales cargados correctamente.
```

### 5. Abrir en el navegador

```
http://localhost:8090
```

### Comandos útiles

| Comando | Descripción |
|---------|-------------|
| `mvn spring-boot:run` | Ejecutar la aplicación |
| `mvn clean install` | Compilar y empaquetar |
| `mvn test` | Ejecutar tests |
| `mvn spring-boot:run -Dspring-boot.run.profiles=dev` | Con perfil específico |
| `java -jar target/veterinary-clinic-management-system-1.0.0.jar` | Ejecutar JAR compilado |

---

## 🔑 Credenciales de Prueba

| Rol | Email | Password | Descripción |
|-----|-------|----------|-------------|
| **ADMIN** | `admin@petclinic.com` | `Admin123` | Acceso total al sistema |
| **VETERINARIO** | `vet@petclinic.com` | `Vet123` | Gestión clínica completa |
| **RECEPCIONISTA** | `recepcion@petclinic.com` | `Recep123` | Lectura y creación básica |
| **CLIENTE** | `ana@email.com` | `Ana123` | Portal del cliente (citas, facturas, historial) |

> **Nota:** Los 3 primeros son de tipo "Personal" en el login. El cliente debe seleccionar "Cliente" como tipo.

### Datos de prueba incluidos

| Tipo | Datos |
|------|-------|
| **Veterinarios** | Admin Sistema, Laura García (Cirugía), Carlos López (Recepción) |
| **Clientes** | Ana Martínez (con portal activo), Pedro Ramírez |
| **Mascotas** | Max (Golden Retriever), Luna (Siamés), Rocky (Bulldog Francés) |
| **Servicios** | Consulta General ($500), Vacunación ($350), Cirugía ($2500), Desparasitación ($200), Grooming ($400) |
| **Medicamentos** | Amoxicilina (100 tabs), Meloxicam (50 ml), Frontline Plus (3 pipetas - stock bajo) |
| **Citas** | 2 citas programadas para el día siguiente |

---

## ✨ Funcionalidades

### Módulo Veterinarios
- CRUD completo de veterinarios con roles (ADMIN, VETERINARIO, RECEPCIONISTA)
- Gestión de horarios y duración de turnos
- Activación/desactivación de cuentas

### Módulo Clientes
- Registro con datos de contacto y dirección
- Búsqueda por nombre, apellido o email
- Portal web para clientes (consultar citas, facturas, historial)

### Módulo Mascotas
- Registro con especie, raza, color, género y peso
- Control de alergias y condiciones médicas
- Vinculación directa con cliente dueño
- Vista unificada: citas, historial, vacunas y planes

### Módulo Citas
- Calendario interactivo con FullCalendar
- Validación de disponibilidad (evita solapamientos)
- Estados: Programada → Confirmada → En Curso → Completada
- Cancelación con motivo
- Notificaciones en tiempo real vía WebSocket
- Drag & drop para reprogramar

### Módulo Servicios
- Catálogo con precio base y código interno
- Activación/desactivación sin eliminar

### Módulo Medicamentos
- Control de stock con alerta de nivel mínimo
- Trazabilidad: cada movimiento registra entrada/salida/ajuste
- Descuento automático al facturar

### Módulo Facturación
- Facturas con items mixtos (servicios + medicamentos)
- Cálculo automático de subtotales, descuentos y total
- Registro de pagos parciales
- Control de saldo pendiente
- Estados: Pendiente → Pagada Parcial → Pagada
- Generación de PDF con iText 7
- Exportación a Excel con Apache POI

### Módulo Historial Médico
- Notas detalladas por consulta
- Diagnóstico, procedimiento y medicación indicada
- Vinculado a citas y mascotas

### Módulo Vacunas
- Registro con fecha de aplicación y próxima dosis
- Control de lote y fabricante

### Módulo Hospitalización
- Ingreso con asignación de jaula
- Control de check-in/check-out
- Estado: Hospitalizado → Dado de Alta

### Módulo Planes de Tratamiento
- Planes con costo estimado y pasos secuenciados
- Seguimiento de avance por paso (Pendiente → En Proceso → Completado)

### Módulo Consentimientos
- Documentos informativos para procedimientos
- Firma digital con registro de fecha y nombre
- Generación de PDF

### Módulo Reportes
- Dashboard con indicadores clave
- Reporte de ingresos por período
- Exportación a Excel

### Módulo Odontograma
- Diagrama dental canino con numeración completa
- Estado por diente: SANO, TÁRTARO, CARIES, FRACTURA, AUSENTE, PERIODONTAL, OBTURADO, ENDODONCIA
- Interfaz visual interactiva en frontend

### Módulo Pagos Online (Stripe)
- Integración con Stripe Payment Intents
- Modal simulado cuando Stripe no está configurado
- Webhook para confirmación de pagos
- Vinculación automática a facturas existentes

### Módulo Reserva Pública
- Endpoints sin autenticación para agendar citas
- Listado de veterinarios disponibles
- Verificación de slots disponibles por fecha
- Validación de email del cliente

### Módulo Notificaciones
- Alertas de vacunación por email (próximas y vencidas)
- Notificaciones automáticas diarias vía `@Scheduled`
- Recordatorios de citas
- Integración con JavaMailSender (SMTP)

### Selector de Temas
- 8 temas de color: Light, Dark, Terracota, Blue Pro, Pink, Purple, Olive, Sunset
- Persistencia en localStorage
- Aplicación en tiempo real sin recarga

### Seguridad
- Autenticación JWT con tokens de 30 minutos
- Roles y permisos granulares
- Contraseñas hasheadas con BCrypt
- Auditoría de acciones con logging síncrono
- Filtro CORS configurado

---

## 🧪 Tests

```bash
mvn test
```

Ejecuta **30 tests de integración** en 2 clases que cubren:
1. Login como Admin y Veterinario
2. Dashboard stats
3. CRUD Clientes, Mascotas
4. Creación y eliminación de Citas
5. Consulta de Servicios y Medicamentos
6. Creación de Factura con descuento de stock
7. Generación de PDF
8. Consulta de Historial Médico y Hospitalizaciones
9. Control de acceso al Portal Cliente
10. Login como Cliente

**Tests adicionales (`NewFeaturesIntegrationTest.java`):**
11. Login Admin y Veterinario
12. Reserva pública (listar veterinarios, slots, crear cita, validación email)
13. Pagos Stripe (crear factura, payment intent con/sin auth, factura inválida)
14. Consulta de vacunas
15. Creación de cita dispara notificación
16. Verificación de beans del contexto
17. Seguridad en endpoints públicos vs autenticados

---

## 📦 Compilación para Producción

```bash
mvn clean package -DskipTests
```

Esto genera el archivo `target/veterinary-clinic-management-system-1.0.0.jar`.

Ejecutar con:
```bash
java -jar target/veterinary-clinic-management-system-1.0.0.jar
```

Para cambiar la configuración sin recompilar:
```bash
java -jar target/veterinary-clinic-management-system-1.0.0.jar \
  --spring.datasource.url=jdbc:postgresql://host:5432/veterinary_db \
  --spring.datasource.username=user \
  --spring.datasource.password=pass
```

---

## 🐳 Docker (opcional)

Crear `Dockerfile`:
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/veterinary-clinic-management-system-1.0.0.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t petclinic .
docker run -p 8090:8090 petclinic
```

---

## 📝 Notas

- El frontend y backend se sirven desde el mismo puerto (8090)
- No se requiere configuración adicional de servidor web
- El schema de BD se crea automáticamente al iniciar
- Los datos de prueba se cargan solo la primera vez (cuando la tabla veterinarios está vacía)
- Para resetear los datos: borrar las tablas y reiniciar

---

## 🤝 Contribución

1. Fork del proyecto
2. Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit (`git commit -m 'Add AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

---

## 📄 Licencia

Distribuido bajo licencia MIT.

---

## ✨ Créditos

Proyecto desarrollado como sistema de gestión para clínicas veterinarias, basado en la arquitectura de dental-clinic-management-system.
