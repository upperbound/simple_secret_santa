package com.github.upperbound.secret_santa.service.impl;

import com.github.upperbound.secret_santa.service.NotificationEvent;
import com.github.upperbound.secret_santa.service.NotificationEventFactory;
import com.github.upperbound.secret_santa.service.NotificationEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadFactory;

/**
 * <p> Provides the default implementation for {@link NotificationEventFactory} </p>
 * @author Vladislav Tsukanov
 */
@Slf4j
@Service
public class DefaultNotificationEventFactory implements NotificationEventFactory {
    private final ThreadFactory threadFactory;
    private final NotificationEventConsumer notificationEventConsumer;
    private final String defaultFrom;
    private final int RESEND_ATTEMPTS;

    public DefaultNotificationEventFactory(NotificationEventConsumer notificationEventConsumer,
                                           @Value("${app.mail.from}")
                                           String defaultFrom,
                                           @Value("${app.mail.resend-attempts:1}")
                                           int resendAttempts)
    {
        this.threadFactory = Thread.ofVirtual()
                .name("send-notification-event", 1L)
                .uncaughtExceptionHandler(
                        (thread, ex) -> log.error("unexpected notification delivery exception", ex)
                )
                .factory();
        this.notificationEventConsumer = notificationEventConsumer;
        this.defaultFrom = defaultFrom;
        this.RESEND_ATTEMPTS = resendAttempts;
    }

    @Override
    public NotificationEvent fireNotification(String to, String subject, String text) {
        return fireNotification(defaultFrom, to, subject, text);
    }

    @Override
    public NotificationEvent fireNotification(String to, String subject, String text, int resendAttempts) {
        return fireNotification(defaultFrom, to, subject, text, resendAttempts);
    }

    public NotificationEvent fireNotification(String from, String to, String subject, String text) {
        return fireNotification(from, to, subject, text, RESEND_ATTEMPTS);
    }

    @Override
    public NotificationEvent fireNotification(String from, String to, String subject, String text, int resendAttempts) {
        NotificationEvent notificationEvent = new DefaultNotificationEvent(from, to, subject, text, resendAttempts);
        threadFactory.newThread(() -> notificationEventConsumer.consume(notificationEvent)).start();
        return notificationEvent;
    }
}
