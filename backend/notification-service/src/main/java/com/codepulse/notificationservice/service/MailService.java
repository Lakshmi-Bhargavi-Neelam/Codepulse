package com.codepulse.notificationservice.service;

/** Thin wrapper around JavaMailSender for sending plain-text alert emails. */
public interface MailService {

    void sendEmail(String to, String subject, String body);
}
