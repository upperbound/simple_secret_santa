package com.github.upperbound.secret_santa.service.impl;

import com.github.upperbound.secret_santa.service.ExceptionCode;
import com.github.upperbound.secret_santa.service.NotificationEvent;
import com.github.upperbound.secret_santa.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <p> Provides the default implementation for {@link NotificationEvent} </p>
 * <p> The {@link #take() take}, {@link #put() put} and {@link #waitForSend waitForSend}
 * methods are mutually synchronized against each other and any {@link #take() take} call
 * requires {@link #put() put} afterwards, so an attempt to initiate a {@link #waitForSend waitForSend} operation
 * will block until {@link #put() put} method will be invoked at least once. </p>
 * @author Vladislav Tsukanov
 */
@Component
public class DefaultNotificationEvent implements NotificationEvent {
    private static int RESEND_ATTEMPTS = 0;
    private final Object lock = new Object();
    private final String from;
    private final String to;
    private final String subject;
    private final String text;
    private int resendAttemptsThreshold;
    private int sendAttempts = 0;
    private Status status = Status.SCHEDULED;
    private ServiceException exception = null;
    private volatile boolean processing = false;

    @Autowired
    public DefaultNotificationEvent(@Value("${app.mail.resend-attempts:1}") int resendAttempts) {
        from = null;
        to = null;
        subject = null;
        text = null;
        RESEND_ATTEMPTS = resendAttempts;
    }

    public DefaultNotificationEvent(String from, String to, String subject, String text, int resendAttemptsThreshold) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.text = text;
        this.resendAttemptsThreshold = resendAttemptsThreshold;
    }

    public DefaultNotificationEvent(NotificationEvent another) {
        this.from = another.getFrom();
        this.to = another.getMessage();
        this.subject = another.getSubject();
        this.text = another.getMessage();
        this.status = another.getStatus();
        this.exception = another.getLastException();
        this.sendAttempts = another.getSendAttempts();
        this.resendAttemptsThreshold = RESEND_ATTEMPTS;
    }


    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getMessage() {
        return text;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public ServiceException getLastException() {
        return exception;
    }

    @Override
    public int getSendAttempts() {
        return sendAttempts;
    }

    @Override
    public boolean sendAttemptsExceeded() {
        return resendAttemptsThreshold != -1 && sendAttempts > 0 && sendAttempts > resendAttemptsThreshold;
    }

    protected boolean take() throws InterruptedException {
        if (status != Status.SENT && !sendAttemptsExceeded()) {
            synchronized (lock) {
                while (status != Status.SENT && !sendAttemptsExceeded()) {
                    if (processing) {
                        lock.wait();
                    } else {
                        return processing = true;
                    }
                }
            }
        }
        return false;
    }

    protected void put() {
        synchronized (lock) {
            lock.notifyAll();
            processing = false;
        }
    }

    protected DefaultNotificationEvent setResendAttemptsThreshold(int resendAttemptsThreshold) {
        this.resendAttemptsThreshold = resendAttemptsThreshold;
        return this;
    }

    protected DefaultNotificationEvent updateStatus(Status status, ServiceException exception) {
        sendAttempts++;
        this.status = status;
        this.exception = exception;
        return this;
    }

    @Override
    public Status waitForSend() throws ServiceException {
        if (status != Status.SENT && !sendAttemptsExceeded()) {
            synchronized (lock) {
                if (status != Status.SENT && !sendAttemptsExceeded()) {
                    try {
                        while (processing) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                        throw new ServiceException(ExceptionCode.EMAIL_DELIVERY_EXCEPTION, to, e);
                    } finally {
                        lock.notify();
                    }
                }
            }
        }
        return status;
    }
}
