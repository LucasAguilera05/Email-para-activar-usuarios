package com.test.email.services;

import com.test.email.services.models.PersContacto;
import com.test.email.services.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Usuarios extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByNombUsr(String nombUsr);

    Optional<Usuario> findByCodpers(Long codpers);
}
