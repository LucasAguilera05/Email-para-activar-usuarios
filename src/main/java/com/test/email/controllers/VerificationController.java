// src/main/java/com/test/email/controllers/VerificationController.java
package com.test.email.controllers;

import com.test.email.services.models.Usuario;
import com.test.email.services.Usuarios;
import com.test.email.services.UsuariosVerificRepository;
import com.test.email.services.models.usuariosVerific;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class VerificationController {

    private final UsuariosVerificRepository verificRepo;
    private final Usuarios usuarioRepo;

    public VerificationController(UsuariosVerificRepository verificRepo, Usuarios usuarioRepo) {
        this.verificRepo = verificRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping(value = "/verify-email-legacy", produces = MediaType.TEXT_HTML_VALUE)
    @Transactional
    public String verifyEmail(@RequestParam("token") String token) {

        usuariosVerific uv = verificRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido."));

        LocalDateTime now = LocalDateTime.now();
        if (uv.getFecExpira() == null || now.isAfter(uv.getFecExpira())) {
            return """
                   <html><head><meta charset="utf-8"><title>Link expirado</title></head>
                   <body style="font-family: Arial, sans-serif">
                     <h2>El enlace de verificación ha expirado</h2>
                     <p>Solicitá un nuevo correo de verificación.</p>
                   </body></html>
                   """;
        }

        String nombUsr = uv.getNombUsr();
        if (nombUsr == null || nombUsr.isBlank()) {
            return """
                   <html><head><meta charset="utf-8"><title>Error</title></head>
                   <body style="font-family: Arial, sans-serif">
                     <h2>Error</h2>
                     <p>No se pudo asociar el token a un usuario (nomb_usr vacío).</p>
                   </body></html>
                   """;
        }

        Usuario usuario = usuarioRepo.findByNombUsr(nombUsr)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + nombUsr));

        if (!usuario.isActivo()) {
            usuario.setActivo(true);
            usuarioRepo.save(usuario);
        }

        uv.setFecVerificado(now);
        verificRepo.save(uv); // opcional: también podrías invalidarlo: uv.setFecExpira(now);

        return """
               <html><head><meta charset="utf-8"><title>Confirmación</title></head>
               <body style="font-family: Arial, sans-serif">
                 <h2>Email confirmado</h2>
                 <p>Usuario activado con éxito.</p>
               </body></html>
               """;
    }
}
