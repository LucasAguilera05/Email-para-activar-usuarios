package com.test.email.services;

import com.test.email.services.models.emailDTO;
import jakarta.mail.MessagingException;

public interface emailService {
    public void sendMail(emailDTO email) throws MessagingException;
}