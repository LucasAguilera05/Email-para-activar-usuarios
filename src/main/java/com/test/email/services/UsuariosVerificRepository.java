package com.test.email.services;

import com.test.email.services.models.UsuarioVerific;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuariosVerificRepository extends JpaRepository<UsuarioVerific, Integer> {
    Optional<UsuarioVerific> findByCodpers(Long codpers);
    Optional<UsuarioVerific> findByToken(String token); // <-- NUEVO

}