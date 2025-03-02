package com.github.upperbound.secret_santa.service;

/**
 * <p> Provides the ability to fire {@link NotificationEvent notification events} </p>
 * @author Vladislav Tsukanov
 */
public interface NotificationEventFactory {
    /**
     * @see #fireNotification(String, String, String, String, int)
     */
    NotificationEvent fireNotification(String to, String subject, String text);

    /**
     * @see #fireNotification(String, String, String, String, int)
     */
    NotificationEvent fireNotification(String to, String subject, String text, int resendAttempts);

    /**
     * @see #fireNotification(String, String, String, String, int)
     */
    NotificationEvent fireNotification(String from, String to, String subject, String text);

    /**
     * @param from the identity of the person who wished this notification to be sent
     * @param to the recipient address
     * @param subject the subject
     * @param text the plain text of the content
     * @param resendAttempts the number of attempts to resend this notification in case of any error
     * @return notification event that will be handled and sent
     * @see NotificationEvent#waitForSend() waitForSend
     */
    NotificationEvent fireNotification(String from, String to, String subject, String text, int resendAttempts);
}
