package com.foodly.notification.store;

import com.foodly.notification.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/** Bounded, thread-safe in-memory buffer of recently delivered notifications. */
@Component
@Slf4j
public class NotificationStore {

    private static final int MAX = 500;

    private final ConcurrentLinkedDeque<Notification> recent = new ConcurrentLinkedDeque<>();

    public void record(Notification notification) {
        recent.addFirst(notification);
        while (recent.size() > MAX) {
            recent.pollLast();
        }
        log.info("NOTIFY [{}] {} → {}", notification.type(), notification.aggregateId(), notification.message());
    }

    public List<Notification> recent() {
        return new ArrayList<>(recent);
    }
}
