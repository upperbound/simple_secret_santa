package com.github.upperbound.secret_santa.service.impl;

import com.github.upperbound.secret_santa.aop.MDCLog;
import com.github.upperbound.secret_santa.service.*;
import com.github.upperbound.secret_santa.util.StaticContext;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * <p> Provides the default implementation for {@link NotificationEventConsumer}
 * to deliver notifications by email. </p>
 * <p> Also exposes corresponding MX bean </p>
 * @author Vladislav Tsukanov
 */
@Slf4j
@ManagedResource(description="Mail event consumer")
@Profile("!ignore-notifications")
@Service
public class MailEventConsumer implements NotificationEventConsumer {
    private final Queue<DefaultNotificationEvent> failedQueue;
    private final JavaMailSender mailSender;

    public MailEventConsumer(JavaMailSender mailSender) {
        this.failedQueue = new ConcurrentLinkedQueue<>();
        this.mailSender = mailSender;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, executionTime = true)
    @Override
    public void consume(NotificationEvent event) {
        DefaultNotificationEvent mailEvent = event instanceof DefaultNotificationEvent ?
                (DefaultNotificationEvent) event :
                new DefaultNotificationEvent(event);
        try {
            MDC.put(StaticContext.MDC_SERVICE_ACTION, "send to <" + mailEvent.getTo() + ">");
            log.info("trying to send mail");
            doSendEmail(mailEvent);
        } catch (ServiceException se) {
            log.error(se.getErrorDescription());
            if (!mailEvent.sendAttemptsExceeded())
                failedQueue.add(mailEvent);
        }
    }

    @ManagedAttribute(defaultValue = "0", description="amount of failed emails waiting to be resent")
    @Override
    public int getFailedQueueSize() {
        return failedQueue.size();
    }

    @Override
    public Collection<NotificationEvent> getAllFailedEvents() {
        return List.copyOf(failedQueue);
    }

    @ManagedOperation(description="remove all failed mail events")
    @Override
    public void clearFailedEvents() {
        failedQueue.forEach(
                defaultNotificationEvent -> defaultNotificationEvent.setResendAttemptsThreshold(0)
                        .updateStatus(defaultNotificationEvent.getStatus(), defaultNotificationEvent.getLastException())
        );
        failedQueue.clear();
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, executionTime = true)
    @Scheduled(initialDelay = 60, fixedDelayString = "${app.mail.resend-delay:30}", timeUnit = TimeUnit.SECONDS)
    public void doSendFailed() {
        failedQueue.forEach(event -> {
            try {
                MDC.put(
                        StaticContext.MDC_SERVICE_ACTION,
                        "resend(" + (event.getSendAttempts() - 1) + ") to <" + event.getTo() + ">"
                );
                log.info("trying to resend mail");
                doSendEmail(event);
            } catch (ServiceException se) {
                log.error(se.getErrorDescription());
            }
        });
        failedQueue.removeIf(NotificationEvent::sendAttemptsExceeded);
    }

    protected void doSendEmail(DefaultNotificationEvent event) throws ServiceException {
        try {
            if (!event.take()) {
                log.warn("mail delivery attempts exceeded");
                return;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message);
            messageHelper.setFrom(event.getFrom());
            messageHelper.setTo(event.getTo());
            messageHelper.setSubject(event.getSubject());
            messageHelper.setText(event.getMessage());
            mailSender.send(message);
            event.updateStatus(NotificationEvent.Status.SENT, null);
            log.info("mail successfully sent");
        } catch (MailException | InterruptedException e) {
            ServiceException se = new ServiceException(ExceptionCode.EMAIL_DELIVERY_EXCEPTION, event.getTo(), e);
            event.updateStatus(NotificationEvent.Status.FAILED, se);
            throw se;
        } catch (MessagingException e) {
            ServiceException se = new ServiceException(ExceptionCode.EMAIL_PARSING_EXCEPTION, event.getTo(), e);
            event.updateStatus(NotificationEvent.Status.FAILED, se);
            throw se;
        } finally {
            event.put();
        }
    }
}
