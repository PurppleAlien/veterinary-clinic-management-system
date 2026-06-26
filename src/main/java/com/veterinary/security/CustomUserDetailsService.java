package com.veterinary.security;

import com.veterinary.domain.Cliente;
import com.veterinary.domain.Veterinario;
import com.veterinary.repository.ClienteRepository;
import com.veterinary.repository.VeterinarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final VeterinarioRepository veterinarioRepository;
    private final ClienteRepository clienteRepository;

    public CustomUserDetailsService(VeterinarioRepository veterinarioRepository,
                                    ClienteRepository clienteRepository) {
        this.veterinarioRepository = veterinarioRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return veterinarioRepository.findByEmail(username)
                .map(vet -> new User(vet.getEmail(), vet.getPasswordHash(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + vet.getRol().name()))))
                .orElseGet(() -> clienteRepository.findByPortalEmail(username)
                        .map(cliente -> new User(cliente.getPortalEmail(), cliente.getPortalPasswordHash(),
                                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username)));
    }

    public UserDetails loadVeterinarioByEmail(String email) {
        Veterinario vet = veterinarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Veterinario no encontrado: " + email));
        return new User(vet.getEmail(), vet.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + vet.getRol().name())));
    }

    public UserDetails loadClienteByEmail(String email) {
        Cliente cliente = clienteRepository.findByPortalEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no encontrado: " + email));
        return new User(cliente.getPortalEmail(), cliente.getPortalPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
    }
}
