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
        if (token != null && !token.isBlank()) {
            UsuarioVerific uv = usuariosVerificRepository.findByToken(token)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido o inexistente."));

            Instant now = Instant.now();
            if (!now.isBefore(uv.getFecExpira())) {

                return htmlOk("Token vencido",
                        "El enlace de verificación ha expirado. Solicitá uno nuevo desde la aplicación.");
            }
            uv.setEmailConfirm(true);

            usuariosVerificRepository.save(uv);
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
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe proporcionar 'token' (preferido) o 'nomb_usr' / 'codpers'.");
    }
    private String htmlOk(String title, String body) {
        return "<html>"
                + "<body style=\"font-family: Arial, sans-serif\">"
                +"<body style=\"text-align: center; font-size: 35px \">"
                + "<h1>" + htmlEscape(title) + "</h1>"
                + "<p>" + htmlEscape(body) + "</p>"
                + "</body>"
                + "</html>";
    }
    private String escape(String s) {
        if (s == null) return "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", " ");
    }
}
