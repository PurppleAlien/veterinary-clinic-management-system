package com.veterinary.repository;

import com.veterinary.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByPortalEmail(String portalEmail);
    Optional<Cliente> findByEmail(String email);
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Cliente> search(String q);
    List<Cliente> findByPortalActivoTrue();
}
