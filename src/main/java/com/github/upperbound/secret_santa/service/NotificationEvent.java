package com.github.upperbound.secret_santa.service;

import jakarta.annotation.Nullable;

/**
 * <p> Event for a notification </p>
 * @author Vladislav Tsukanov
 * @see NotificationEventFactory
 * @see #waitForSend() waitForSend
 */
public interface NotificationEvent {
    /**
     * Notification event status
     */
    enum Status{SCHEDULED, SENT, FAILED};

    /**
     * @return the identity of the person who wished this event to be sent.
     */
    String getFrom();

    /**
     * @return the recipient address
     */
    String getTo();

    /**
     * @return the subject
     */
    String getSubject();

    /**
     * @return the plain text of the content of this notification
     */
    String getMessage();

    /**
     * @return current {@link Status}
     */
    Status getStatus();

    /**
     * @return the last exception from the last sending attempt or {@code null}
     */
    @Nullable
    ServiceException getLastException();

    /**
     * @return the number of attempts this notification have been sent
     */
    int getSendAttempts();

    /**
     * @return {@code true} if the number of attempts to send the notification has been exceeded, {@code false} otherwise
     */
    boolean sendAttemptsExceeded();

    /**
     * <p> Waits for the next attempt to send this notification only if the number of attempts has not been exceeded
     * and if the notification has not been successfully sent, otherwise returns immediately </p>
     * @return status of the last attempt
     * @throws ServiceException if that thread was interrupted
     */
    Status waitForSend() throws ServiceException;
}
