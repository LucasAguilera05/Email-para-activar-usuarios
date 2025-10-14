package com.test.email.services;

import com.test.email.services.models.PersContacto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersContactoRepository extends JpaRepository<PersContacto, Long> {
    Optional<PersContacto> findByCodpers(Long codpers);
}
