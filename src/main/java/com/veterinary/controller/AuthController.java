package com.veterinary.controller;

import com.veterinary.domain.Cliente;
import com.veterinary.domain.Veterinario;
import com.veterinary.dto.LoginRequest;
import com.veterinary.dto.LoginResponse;
import com.veterinary.repository.ClienteRepository;
import com.veterinary.repository.VeterinarioRepository;
import com.veterinary.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final VeterinarioRepository veterinarioRepo;
    private final ClienteRepository clienteRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtProvider;

    public AuthController(VeterinarioRepository veterinarioRepo, ClienteRepository clienteRepo,
                          PasswordEncoder encoder, JwtTokenProvider jwtProvider) {
        this.veterinarioRepo = veterinarioRepo;
        this.clienteRepo = clienteRepo;
        this.encoder = encoder;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        if ("CLIENTE".equals(req.getTipo())) {
            Cliente c = clienteRepo.findByPortalEmail(req.getEmail())
                    .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
            if (!encoder.matches(req.getPassword(), c.getPortalPasswordHash())) {
                throw new RuntimeException("Credenciales inválidas");
            }
            if (!c.getPortalActivo()) {
                throw new RuntimeException("Portal de cliente desactivado");
            }
            String token = jwtProvider.generateToken(c.getPortalEmail(), "CLIENTE", c.getId());
            return ResponseEntity.ok(new LoginResponse(token, c.getPortalEmail(),
                    c.getNombre() + " " + c.getApellido(), "CLIENTE", c.getId()));
        } else {
            Veterinario v = veterinarioRepo.findByEmail(req.getEmail())
                    .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
            if (!encoder.matches(req.getPassword(), v.getPasswordHash())) {
                throw new RuntimeException("Credenciales inválidas");
            }
            if (!v.getActivo()) {
                throw new RuntimeException("Usuario desactivado");
            }
            String token = jwtProvider.generateToken(v.getEmail(), v.getRol().name(), v.getId());
            return ResponseEntity.ok(new LoginResponse(token, v.getEmail(),
                    v.getNombre() + " " + v.getApellido(), v.getRol().name(), v.getId()));
        }
    }
}
