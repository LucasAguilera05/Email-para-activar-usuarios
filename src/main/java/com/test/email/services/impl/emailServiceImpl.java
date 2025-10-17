package com.test.email.services.impl;

import com.test.email.services.UsuariosVerificRepository;
import com.test.email.services.emailService;
import com.test.email.services.models.PersContacto;
import com.test.email.services.models.UsuarioVerific;
import com.test.email.services.models.emailDTO;
import com.test.email.services.PersContactoRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class emailServiceImpl implements emailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final PersContactoRepository contactoRepo;
    private final UsuariosVerificRepository usuariosVerificRepo;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.base-url:}")
    private String baseUrl;

    public emailServiceImpl(JavaMailSender javaMailSender,
                            TemplateEngine templateEngine,
                            PersContactoRepository contactoRepo,
                            UsuariosVerificRepository usuariosVerificRepo) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.contactoRepo = contactoRepo;
        this.usuariosVerificRepo = usuariosVerificRepo;
    }

    @Override
    public void sendMail(emailDTO email) throws MessagingException {
        try {
            System.out.println("[EMAIL] INICIO sendMail");
            System.out.println("[EMAIL] baseUrl=" + baseUrl);
            System.out.println("[EMAIL] mailFrom=" + mailFrom);
            System.out.println("[EMAIL] entrada codpers=" + email.getCodpers()
                    + " nomb_usr=" + email.getNomb_usr()
                    + " destinatario=" + email.getDestinatario());

            if (baseUrl == null || baseUrl.isBlank()) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "app.base-url no está configurado");
            }
            if (mailFrom == null || mailFrom.isBlank()) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "spring.mail.username no está configurado");
            }

            String destinatario = email.getDestinatario();
            if (destinatario == null || destinatario.isBlank()) {
                Long codpers = email.getCodpers();
                if (codpers == null) {
                    throw new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.BAD_REQUEST,
                            "Falta 'destinatario' o 'codpers'.");
                }
                PersContacto pc = contactoRepo.findByCodpers(codpers)
                        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "No existe persxcontacto con codpers=" + codpers));
                String c1 = pc.getCorreo1(), c2 = pc.getCorreo2();
                destinatario = (c1 != null && !c1.isBlank()) ? c1 : c2;
                if (destinatario == null || destinatario.isBlank()) {
                    throw new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.CONFLICT,
                            "El usuario " + codpers + " no tiene correo1/correo2 cargado.");
                }
            }
            System.out.println("[EMAIL] destinatario=" + destinatario);

            String verificationLink;
            if (email.getNomb_usr() != null && !email.getNomb_usr().isBlank()) {
                verificationLink = baseUrl + "/api/verify-email?nomb_usr=" +
                        java.net.URLEncoder.encode(email.getNomb_usr(), java.nio.charset.StandardCharsets.UTF_8);
            } else if (email.getCodpers() != null) {
                Long codpers = email.getCodpers();
                UsuarioVerific uv = usuariosVerificRepo.findByCodpers(codpers)
                        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "No existe usuarios_verific con codpers=" + codpers));

                String token = uv.getToken();
                System.out.println("[EMAIL] token obtenido=" + token + " para codpers=" + codpers);
                if (token == null || token.isBlank()) {
                    throw new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.CONFLICT,
                            "El token está nulo/vacío en usuarios_verific para codpers=" + codpers);
                }

                verificationLink = baseUrl + "/api/verify-email?token=" +
                        java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
            } else {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Falta 'nomb_usr' o 'codpers' para generar el link.");
            }
            System.out.println("[EMAIL] verificationLink=" + verificationLink);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(destinatario);
            helper.setSubject(email.getAsunto() != null ? email.getAsunto() : "Verificación de email");

            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
            ctx.setVariable("message", email.getMensaje() != null ? email.getMensaje() : "Hacé clic para verificar tu email.");
            ctx.setVariable("verificationLink", verificationLink);

            String contentHTML;
            try {
                contentHTML = templateEngine.process("email", ctx); // plantilla "email.html"
            } catch (Exception te) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al procesar plantilla 'email': " + te.getClass().getSimpleName() + " - " + te.getMessage(), te);
            }
            helper.setText(contentHTML, true);

            try {
                javaMailSender.send(message);
            } catch (Exception me) {
                // Errores típicos: autenticación SMTP, host, puerto, etc.
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al enviar correo: " + me.getClass().getSimpleName() + " - " + me.getMessage(), me);
            }

            System.out.println("[EMAIL] FIN sendMail OK");

        } catch (org.springframework.web.server.ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Fallo inesperado en sendMail: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }
    }
}

