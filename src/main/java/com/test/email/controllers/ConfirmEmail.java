package com.test.email.controllers;

import com.test.email.services.Usuarios;
import com.test.email.services.UsuariosVerificRepository;
import com.test.email.services.models.Usuario;
import com.test.email.services.models.UsuarioVerific;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.TimeZone;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

@RestController
@RequestMapping("/api")
public class ConfirmEmail {

    private final Usuarios usuarioRepository;
    private final UsuariosVerificRepository usuariosVerificRepository;

    public ConfirmEmail(Usuarios usuarioRepository,
                        UsuariosVerificRepository usuariosVerificRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuariosVerificRepository = usuariosVerificRepository;
    }

    @GetMapping(value = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
    @Transactional
    public String verifyEmail(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "nomb_usr", required = false) String nombUsr,
            @RequestParam(value = "codpers", required = false) Long codpersLegacy
    ) {
        // 1) Flujo nuevo por token (prioritario)
        if (token != null && !token.isBlank()) {
            UsuarioVerific uv = usuariosVerificRepository.findByToken(token)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido o inexistente."));

            Instant now = Instant.now();
// Expirado si now es igual o posterior a fec_expira
            if (!now.isBefore(uv.getFecExpira())) {

                return htmlOk("Token vencido",
                        "El enlace de verificación ha expirado. Solicitá uno nuevo desde la aplicación.");
            }

            uv.setEmailConfirm(true);

            usuariosVerificRepository.save(uv);

            // Activar usuario por codpers asociado
            Long codpers = uv.getCodpers();
            if (codpers == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El token no tiene codpers asociado.");
            }

            Usuario usuario = usuarioRepository.findByCodpers(codpers)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Usuario no encontrado para codpers=" + codpers));

            if (!usuario.isActivo()) {
                usuario.setActivo(true);
                usuarioRepository.save(usuario);
            }

            return htmlOk("Email confirmado", "Usuario activado con éxito.");
        }

        // 2) Flujo anterior por nombre de usuario (compatibilidad)
        if (nombUsr != null && !nombUsr.isBlank()) {
            Usuario usuario = usuarioRepository.findByNombUsr(nombUsr)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Usuario no encontrado: " + nombUsr));

            if (!usuario.isActivo()) {
                usuario.setActivo(true);
                usuarioRepository.save(usuario);
            }
            return htmlOk("Email confirmado", "Usuario activado con éxito (por nombre de usuario).");
        }

        // 3) Flujo legacy por codpers (si todavía hay links viejos circulando)
        if (codpersLegacy != null) {
            Usuario usuario = usuarioRepository.findByCodpers(codpersLegacy)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Usuario no encontrado para codpers: " + codpersLegacy));

            if (!usuario.isActivo()) {
                usuario.setActivo(true);
                usuarioRepository.save(usuario);
            }
            return htmlOk("Email confirmado", "Usuario activado con éxito (legacy por codpers).");
        }

        // 4) Si no vino nada válido:
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe proporcionar 'token' (preferido) o 'nomb_usr' / 'codpers'.");
    }

    // Pequeña ayuda para responder HTML simple
    private String htmlOk(String title, String body) {
        return "<html>"
                + "<body style=\"font-family: Arial, sans-serif\">"
                + "<h2>" + htmlEscape(title) + "</h2>"
                + "<p>" + htmlEscape(body) + "</p>"
                + "</body>"
                + "</html>";
    }


    private String escape(String s) {
        if (s == null) return "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", " ");
    }
}
