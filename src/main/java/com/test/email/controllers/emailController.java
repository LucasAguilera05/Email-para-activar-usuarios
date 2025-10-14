package com.test.email.controllers;

import com.test.email.services.emailService;
import com.test.email.services.models.emailDTO;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class emailController {
    @Autowired
    emailService emailService;
    @PostMapping("/email")
    private ResponseEntity<String> sendMail(@RequestBody emailDTO email) throws MessagingException {
        emailService.sendMail(email);
        return new ResponseEntity<>("email enviado exitosamente", HttpStatus.OK);

    }
}
