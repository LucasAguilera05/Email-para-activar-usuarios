package com.test.email.controllers;

import com.test.email.services.Usuarios;
import com.test.email.services.models.Usuario;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ConfirmEmail {

    private Usuarios usuarioRepository;

    public void VerificationController(Usuarios usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public ConfirmEmail(Usuarios usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping(value = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
    public String verifyEmail(
            @RequestParam(value = "nomb_usr", required = false) String nombUsr,
            @RequestParam(value = "codpers", required = false) Long codpers) {

        Usuario usuario;
        if (nombUsr != null && !nombUsr.isBlank()) {
            usuario = usuarioRepository.findByNombUsr(nombUsr)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + nombUsr));
        } else if (codpers != null) {

            usuario = usuarioRepository.findByCodpers(codpers)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para codpers: " + codpers));
        } else {
            throw new IllegalArgumentException("Debe proporcionar 'nomb_usr' o 'codpers'.");
        }

        if (!usuario.isActivo()) {
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
        }

        return """
               <html>
                 <head><meta charset="utf-8"><title>Confirmación</title></head>
                 <body style="font-family: Arial, sans-serif">
                   <h2>Email confirmado</h2>
                   <p>Usuario activado con éxito.</p>
                 </body>
               </html>
               """;
    }
}
