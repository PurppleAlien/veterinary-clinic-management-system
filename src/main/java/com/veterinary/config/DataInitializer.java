package com.veterinary.config;

import com.veterinary.domain.*;
import com.veterinary.domain.enums.*;
import com.veterinary.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final VeterinarioRepository veterinarioRepo;
    private final ClienteRepository clienteRepo;
    private final MascotaRepository mascotaRepo;
    private final ServicioRepository servicioRepo;
    private final MedicamentoRepository medicamentoRepo;
    private final CitaRepository citaRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(VeterinarioRepository veterinarioRepo, ClienteRepository clienteRepo,
                           MascotaRepository mascotaRepo, ServicioRepository servicioRepo,
                           MedicamentoRepository medicamentoRepo, CitaRepository citaRepo,
                           PasswordEncoder encoder) {
        this.veterinarioRepo = veterinarioRepo;
        this.clienteRepo = clienteRepo;
        this.mascotaRepo = mascotaRepo;
        this.servicioRepo = servicioRepo;
        this.medicamentoRepo = medicamentoRepo;
        this.citaRepo = citaRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (veterinarioRepo.count() > 0) return;

        Veterinario admin = new Veterinario();
        admin.setNombre("Admin");
        admin.setApellido("Sistema");
        admin.setEmail("admin@petclinic.com");
        admin.setPasswordHash(encoder.encode("Admin123"));
        admin.setRol(RolUsuario.ADMIN);
        admin.setActivo(true);
        veterinarioRepo.save(admin);

        Veterinario vet1 = new Veterinario();
        vet1.setNombre("Laura");
        vet1.setApellido("García");
        vet1.setEmail("vet@petclinic.com");
        vet1.setPasswordHash(encoder.encode("Vet123"));
        vet1.setRol(RolUsuario.VETERINARIO);
        vet1.setEspecialidad("Cirugía");
        vet1.setCedulaProfesional("VET-12345");
        vet1.setHorarioInicio(LocalTime.of(9, 0));
        vet1.setHorarioFin(LocalTime.of(18, 0));
        vet1.setDuracionTurnoMinutos(30);
        vet1.setActivo(true);
        veterinarioRepo.save(vet1);

        Veterinario recep = new Veterinario();
        recep.setNombre("Carlos");
        recep.setApellido("López");
        recep.setEmail("recepcion@petclinic.com");
        recep.setPasswordHash(encoder.encode("Recep123"));
        recep.setRol(RolUsuario.RECEPCIONISTA);
        recep.setActivo(true);
        veterinarioRepo.save(recep);

        Cliente c1 = new Cliente();
        c1.setNombre("Ana");
        c1.setApellido("Martínez");
        c1.setEmail("ana@email.com");
        c1.setTelefono("555-0101");
        c1.setDireccion("Calle Principal #456");
        c1.setPortalEmail("ana@email.com");
        c1.setPortalPasswordHash(encoder.encode("Ana123"));
        c1.setPortalActivo(true);
        clienteRepo.save(c1);

        Cliente c2 = new Cliente();
        c2.setNombre("Pedro");
        c2.setApellido("Ramírez");
        c2.setEmail("pedro@email.com");
        c2.setTelefono("555-0202");
        c2.setDireccion("Av. Secundaria #789");
        clienteRepo.save(c2);

        Mascota m1 = new Mascota();
        m1.setNombre("Max");
        m1.setEspecie(Especie.CANINO);
        m1.setRaza("Golden Retriever");
        m1.setColor("Dorado");
        m1.setGenero(GeneroMascota.MACHO);
        m1.setFechaNacimiento(LocalDate.of(2020, 3, 15));
        m1.setPeso(32.5);
        m1.setCliente(c1);
        mascotaRepo.save(m1);

        Mascota m2 = new Mascota();
        m2.setNombre("Luna");
        m2.setEspecie(Especie.FELINO);
        m2.setRaza("Siamés");
        m2.setColor("Gris");
        m2.setGenero(GeneroMascota.HEMBRA);
        m2.setFechaNacimiento(LocalDate.of(2021, 7, 20));
        m2.setPeso(4.2);
        m2.setCliente(c1);
        mascotaRepo.save(m2);

        Mascota m3 = new Mascota();
        m3.setNombre("Rocky");
        m3.setEspecie(Especie.CANINO);
        m3.setRaza("Bulldog Francés");
        m3.setColor("Atigrado");
        m3.setGenero(GeneroMascota.MACHO);
        m3.setFechaNacimiento(LocalDate.of(2022, 1, 10));
        m3.setPeso(12.8);
        m3.setCliente(c2);
        mascotaRepo.save(m3);

        Servicio s1 = new Servicio();
        s1.setNombre("Consulta General");
        s1.setDescripcion("Revisión médica general");
        s1.setPrecioBase(500.0);
        s1.setCodigoInterno("CG-001");
        s1.setActivo(true);
        servicioRepo.save(s1);

        Servicio s2 = new Servicio();
        s2.setNombre("Vacunación");
        s2.setDescripcion("Aplicación de vacunas");
        s2.setPrecioBase(350.0);
        s2.setCodigoInterno("VAC-001");
        s2.setActivo(true);
        servicioRepo.save(s2);

        Servicio s3 = new Servicio();
        s3.setNombre("Cirugía");
        s3.setDescripcion("Procedimientos quirúrgicos");
        s3.setPrecioBase(2500.0);
        s3.setCodigoInterno("CIR-001");
        s3.setActivo(true);
        servicioRepo.save(s3);

        Servicio s4 = new Servicio();
        s4.setNombre("Desparasitación");
        s4.setDescripcion("Desparasitación interna y externa");
        s4.setPrecioBase(200.0);
        s4.setCodigoInterno("DES-001");
        s4.setActivo(true);
        servicioRepo.save(s4);

        Servicio s5 = new Servicio();
        s5.setNombre("Estética/Grooming");
        s5.setDescripcion("Baño, corte de uñas y peluquería");
        s5.setPrecioBase(400.0);
        s5.setCodigoInterno("EST-001");
        s5.setActivo(true);
        servicioRepo.save(s5);

        Medicamento med1 = new Medicamento();
        med1.setNombre("Amoxicilina");
        med1.setDescripcion("Antibiótico de amplio espectro");
        med1.setUnidad("tableta");
        med1.setStockActual(100);
        med1.setStockMinimo(10);
        med1.setPrecioUnitario(15.0);
        medicamentoRepo.save(med1);

        Medicamento med2 = new Medicamento();
        med2.setNombre("Meloxicam");
        med2.setDescripcion("Antiinflamatorio");
        med2.setUnidad("ml");
        med2.setStockActual(50);
        med2.setStockMinimo(5);
        med2.setPrecioUnitario(25.0);
        medicamentoRepo.save(med2);

        Medicamento med3 = new Medicamento();
        med3.setNombre("Frontline Plus");
        med3.setDescripcion("Antipulgas y garrapatas");
        med3.setUnidad("pipeta");
        med3.setStockActual(3);
        med3.setStockMinimo(5);
        med3.setPrecioUnitario(180.0);
        medicamentoRepo.save(med3);

        Cita cita1 = new Cita();
        cita1.setMascota(m1);
        cita1.setVeterinario(vet1);
        cita1.setCliente(c1);
        cita1.setFechaHoraInicio(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        cita1.setFechaHoraFin(LocalDateTime.now().plusDays(1).withHour(10).withMinute(30));
        cita1.setMotivo("Revisión anual");
        cita1.setEstado(EstadoCita.PROGRAMADA);
        citaRepo.save(cita1);

        Cita cita2 = new Cita();
        cita2.setMascota(m3);
        cita2.setVeterinario(vet1);
        cita2.setCliente(c2);
        cita2.setFechaHoraInicio(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0));
        cita2.setFechaHoraFin(LocalDateTime.now().plusDays(1).withHour(11).withMinute(30));
        cita2.setMotivo("Vacunación");
        cita2.setEstado(EstadoCita.PROGRAMADA);
        citaRepo.save(cita2);

        System.out.println("Datos iniciales cargados correctamente.");
    }
}
