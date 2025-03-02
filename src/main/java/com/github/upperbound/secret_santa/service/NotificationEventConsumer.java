package com.github.upperbound.secret_santa.service;

import java.util.Collection;

/**
 * <p> Handles any incoming {@link NotificationEvent notification event} by trying to deliver it </p>
 * @author Vladislav Tsukanov
 */
public interface NotificationEventConsumer {
    /**
     * Consumes the event and tries to deliver it
     * @param event event with all necessary data to be sent
     */
    void consume(NotificationEvent event);

    /**
     * @return the number of events that were not sent due to failure
     */
    int getFailedQueueSize();

    /**
     * @return all failed events
     */
    Collection<NotificationEvent> getAllFailedEvents();

    /**
     * clear all failed events
     */
    void clearFailedEvents();
}
