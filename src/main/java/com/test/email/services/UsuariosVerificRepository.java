package com.test.email.services;

import java.util.List;
import java.util.Optional;

import com.test.email.services.models.usuariosVerific;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UsuariosVerificRepository extends JpaRepository<usuariosVerific, Long> {

    Optional<usuariosVerific> findByToken(String token);
    List<usuariosVerific> findByNombUsr(String nombUsr);  // <-- usamos este en el envío
}
