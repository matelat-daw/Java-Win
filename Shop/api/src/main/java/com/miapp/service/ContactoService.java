package com.miapp.service;

import com.miapp.dto.ContactoRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class ContactoService {

    private static final Logger log = LoggerFactory.getLogger(ContactoService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String toEmail;

    public ContactoService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String fromEmail,
            @Value("${tienda.contact.to:}") String toEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail == null ? "" : fromEmail.trim();
        this.toEmail = toEmail == null ? "" : toEmail.trim();
    }

    public void enviar(ContactoRequestDto request) {
        if (fromEmail.isBlank() || toEmail.isBlank()) {
            throw new IllegalStateException("Email no configurado en el servidor");
        }

        String nombre = sanitizeHeader(request.nombre());
        String email = sanitizeHeader(request.email());
        String asunto = "Contacto Tienda Tech - " + nombre;

        String cuerpo = """
                Nuevo mensaje desde la página de contacto

                Nombre: %s
                Email: %s

                Mensaje:
                %s
                """.formatted(nombre, email, request.mensaje());

        try {
            mailSender.send(buildInternalMessage(asunto, cuerpo, email));
            mailSender.send(buildConfirmationMessage(nombre, email));
            log.info("Contacto enviado: to={}, replyTo={}", toEmail, email);
        } catch (MailException e) {
            log.warn("Falló el envío de contacto (SMTP): to={}, replyTo={}", toEmail, email);
            throw new IllegalStateException("No se pudo enviar el email");
        }
    }

    private MimeMessage buildInternalMessage(String subject, String body, String replyTo) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setReplyTo(replyTo);
            helper.setSubject(subject);
            helper.setText(body, false);
            return mimeMessage;
        } catch (MessagingException e) {
            throw new IllegalStateException("No se pudo construir el email");
        }
    }

    private MimeMessage buildConfirmationMessage(String nombre, String toUserEmail) {
        String safeNombre = nombre == null || nombre.isBlank() ? "Hola" : nombre;
        String safeTo = sanitizeHeader(toUserEmail);
        String subject = "Hemos recibido tu mensaje";
        String body = """
                %s,

                Hemos recibido tu mensaje y nos pondremos en contacto contigo en breve.

                Este correo es una confirmación automática.
                """.formatted(safeNombre);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(safeTo);
            helper.setSubject(subject);
            helper.setText(body, false);
            return mimeMessage;
        } catch (MessagingException e) {
            throw new IllegalStateException("No se pudo construir el email");
        }
    }

    private static String sanitizeHeader(String value) {
        if (value == null) return "";
        return value.replace("\r", " ").replace("\n", " ").trim();
    }
}
