package com.algangi.mongle.auth.application.service.email;

public interface MailSender {

    void send(String to, String subject, String text);

}
