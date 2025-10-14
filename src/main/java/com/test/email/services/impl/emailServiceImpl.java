package com.test.email.services.impl;

import com.test.email.services.emailService;
import com.test.email.services.models.PersContacto;
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

@Service
public class emailServiceImpl implements emailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final PersContactoRepository contactoRepo;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.base-url}")
    private String baseUrl;

    public emailServiceImpl(JavaMailSender javaMailSender,
                            TemplateEngine templateEngine,
                            PersContactoRepository contactoRepo) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.contactoRepo = contactoRepo;
    }

    @Override
    public void sendMail(emailDTO email) throws MessagingException {
        try {
            // 1) Resolver destinatario (si no viene, buscar en persxcontacto por codpers)
            String destinatario = email.getDestinatario();
            if (destinatario == null || destinatario.isBlank()) {
                if (email.getCodpers() == null) {
                    throw new IllegalArgumentException("Falta 'destinatario' o 'codpers' para enviar el email.");
                }
                PersContacto pc = contactoRepo.findByCodpers(email.getCodpers())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No existe persxcontacto con codpers=" + email.getCodpers()));
                String c1 = pc.getCorreo1(), c2 = pc.getCorreo2();
                destinatario = (c1 != null && !c1.isBlank()) ? c1 : c2;
                if (destinatario == null || destinatario.isBlank()) {
                    throw new IllegalArgumentException(
                            "El usuario " + email.getCodpers() + " no tiene correo1/correo2 cargado.");
                }
            }

            String verificationLink;
            if (email.getNomb_usr() != null && !email.getNomb_usr().isBlank()) {
                verificationLink = baseUrl + "/api/verify-email?nomb_usr=" + email.getNomb_usr();
            } else if (email.getCodpers() != null) {
                verificationLink = baseUrl + "/api/verify-email?codpers=" + email.getCodpers();
            } else {
                throw new IllegalArgumentException("Falta 'nomb_usr' o 'codpers' para generar el link de verificación.");
            }

           // Preparar correo (Thymeleaf)
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
            ctx.setVariable("verificationLink", verificationLink); // <-- usarlo en la plantilla
            String contentHTML = templateEngine.process("email", ctx);
            helper.setText(contentHTML, true);


            javaMailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("error al enviar email: ", e);
        }
    }
}
