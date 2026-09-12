package com.codepulse.notificationservice.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.codepulse.notificationservice.exception.EmailDeliveryException;
import com.codepulse.notificationservice.service.MailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception ex) {
            log.error("Email delivery failed to {}: {}", to, ex.getMessage());
            throw new EmailDeliveryException("Failed to send email to " + to, ex);
        }
    }
}
