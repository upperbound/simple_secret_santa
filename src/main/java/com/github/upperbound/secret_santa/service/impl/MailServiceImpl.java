package com.github.upperbound.secret_santa.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.github.upperbound.secret_santa.service.ExceptionCode;
import com.github.upperbound.secret_santa.service.MailService;
import com.github.upperbound.secret_santa.service.ServiceException;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    @Value("${app.mail.from:grandfather_frost_no_reply@e-soft.ru}")
    private String defaultFrom;
    @Value("${app.mail.do-send:false}")
    private boolean doSend;

    public void sendMessage(String to, String subject, String text) throws ServiceException {
        sendMessage(defaultFrom, to, subject, text);
    }

    public void sendMessage(String from, String to, String subject, String text) throws ServiceException {
        try {
            if (!doSend)
                return;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(text);
            mailSender.send(message);
        } catch (MessagingException e) {
            ServiceException se = new ServiceException(ExceptionCode.EMAIL_PARSING_EXCEPTION, to, e);
            log.error(se.getErrorDescription(), e);
            throw se;
        } catch (MailException e) {
            ServiceException se = new ServiceException(ExceptionCode.EMAIL_DELIVERY_EXCEPTION, to, e);
            log.error(se.getErrorDescription(), e);
            throw se;
        }
    }
}
