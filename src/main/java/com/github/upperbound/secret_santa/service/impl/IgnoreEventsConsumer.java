package com.github.upperbound.secret_santa.service.impl;

import com.github.upperbound.secret_santa.service.NotificationEvent;
import com.github.upperbound.secret_santa.service.NotificationEventConsumer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * <p> This implementation ignores all notification events and does not perform the actual attempt to deliver it </p>
 * @author Vladislav Tsukanov
 */
@Profile("ignore-notifications")
@Service
public class IgnoreEventsConsumer implements NotificationEventConsumer {
    @Override
    public void consume(NotificationEvent event) {
        if (event instanceof DefaultNotificationEvent me) {
            try {
                me.take();
            } catch (InterruptedException ignored) {
            } finally {
                me.setResendAttemptsThreshold(0)
                        .updateStatus(NotificationEvent.Status.SENT, null);
                me.put();
            }
        }
    }

    @Override
    public int getFailedQueueSize() {
        return 0;
    }

    @Override
    public Collection<NotificationEvent> getAllFailedEvents() {
        return List.of();
    }

    @Override
    public void clearFailedEvents() {}
}
