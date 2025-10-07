package com.algangi.mongle.auth.application.service;

public interface MailSender {

    void send(String to, String subject, String text);

}
