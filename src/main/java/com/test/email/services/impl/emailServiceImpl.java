package com.test.email.services.impl;

import com.test.email.services.emailService;
import com.test.email.services.models.PersContacto;
import com.test.email.services.models.Usuario;
import com.test.email.services.models.emailDTO;
import com.test.email.services.PersContactoRepository;
import com.test.email.services.models.usuariosVerific;
import com.test.email.services.UsuariosVerificRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Comparator;
import com.test.email.services.Usuarios;

@Service
public class emailServiceImpl implements emailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final PersContactoRepository contactoRepo;
    private final UsuariosVerificRepository verificRepo;
    private final Usuarios usuarioRepo;


    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.base-url}")
    private String baseUrl;

    public emailServiceImpl(JavaMailSender javaMailSender,
                            TemplateEngine templateEngine,
                            PersContactoRepository contactoRepo,
                            UsuariosVerificRepository verificRepo,
                            Usuarios usuarioRepo) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.contactoRepo = contactoRepo;
        this.verificRepo = verificRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public void sendMail(emailDTO email) throws MessagingException {
        try {
            // -------- 1) Resolver destinatario ----------
            String destinatario = email.getDestinatario();
            if (destinatario == null || destinatario.isBlank()) {
                // Preferimos nomb_usr -> usuarios -> codpers -> persxcontacto
                if (email.getNomb_usr() != null && !email.getNomb_usr().isBlank()) {

                    // 1.a) nomb_usr -> usuarios (para obtener codpers)
                    Usuario usuario = usuarioRepo.findByNombUsr(email.getNomb_usr())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "No existe usuario con nomb_usr=" + email.getNomb_usr()));

                    Long codpers = usuario.getCodpers();
                    if (codpers == null) {
                        throw new IllegalArgumentException("El usuario " + email.getNomb_usr()
                                + " no tiene 'codpers' asociado; no se puede resolver el correo.");
                    }

                    // 1.b) codpers -> persxcontacto (para obtener correo1/correo2)
                    PersContacto pc = contactoRepo.findByCodpers(codpers)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "No existe persxcontacto con codpers=" + codpers));

                    String c1 = pc.getCorreo1(), c2 = pc.getCorreo2();
                    destinatario = (c1 != null && !c1.isBlank()) ? c1 : c2;

                } else if (email.getCodpers() != null) {
                    // Alternativa: si mandan codpers directo, también funciona
                    PersContacto pc = contactoRepo.findByCodpers(email.getCodpers())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "No existe persxcontacto con codpers=" + email.getCodpers()));
                    String c1 = pc.getCorreo1(), c2 = pc.getCorreo2();
                    destinatario = (c1 != null && !c1.isBlank()) ? c1 : c2;

                } else {
                    // Ni destinatario, ni nomb_usr, ni codpers
                    throw new IllegalArgumentException(
                            "Falta 'destinatario' o, en su defecto, 'nomb_usr' (o 'codpers') para resolver el correo."
                    );
                }

                if (destinatario == null || destinatario.isBlank()) {
                    throw new IllegalArgumentException(
                            "No hay correo1/correo2 cargado para el usuario especificado."
                    );
                }
            }

            // -------- 2) Resolver TOKEN vigente por nomb_usr ----------
            if (email.getNomb_usr() == null || email.getNomb_usr().isBlank()) {
                throw new IllegalArgumentException("Falta 'nomb_usr' para buscar el token de verificación.");
            }
            LocalDateTime now = LocalDateTime.now();
            usuariosVerific uvVigente = verificRepo.findByNombUsr(email.getNomb_usr()).stream()
                    .filter(uv -> uv.getFecExpira() != null && now.isBefore(uv.getFecExpira()))
                    .max(Comparator.comparing(usuariosVerific::getFecExpira))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No hay token vigente para nomb_usr=" + email.getNomb_usr() + "."));

            String token = uvVigente.getToken();

            // -------- 3) Link con token ----------
            String verificationLink = baseUrl + "/api/verify-email?token=" + token;

            // -------- 4) Preparar correo (Thymeleaf) ----------
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (mailFrom == null || mailFrom.isBlank()) {
                throw new IllegalStateException("spring.mail.username no está configurado.");
            }
            helper.setFrom(mailFrom);
            helper.setTo(destinatario);
            helper.setSubject(email.getAsunto());

            Context ctx = new Context();
            ctx.setVariable("message", email.getMensaje());
            ctx.setVariable("verificationLink", verificationLink);
            String contentHTML = templateEngine.process("email", ctx);
            helper.setText(contentHTML, true);

            // -------- 5) Enviar ----------
            javaMailSender.send(message);

        } catch (IllegalArgumentException e) {
            throw e; // para que tu @RestControllerAdvice lo devuelva como 400 con el mensaje claro
        } catch (Exception e) {
            throw new RuntimeException("error al enviar email: ", e);
        }
    }
}
