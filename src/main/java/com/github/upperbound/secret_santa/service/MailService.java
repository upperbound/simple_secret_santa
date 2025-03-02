package com.github.upperbound.secret_santa.service;

public interface MailService {
    void sendMessage(String to, String subject, String text) throws ServiceException;
    void sendMessage(String from, String to, String subject, String text) throws ServiceException;
}
