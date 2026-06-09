package com.foodly.notification.store;

import com.foodly.notification.model.Notification;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationStoreTest {

    @Test
    void record_keepsMostRecentFirst() {
        NotificationStore store = new NotificationStore();
        store.record(Notification.of("order.placed", "order-1", "placed"));
        store.record(Notification.of("payment.completed", "order-1", "confirmed"));

        List<Notification> recent = store.recent();

        assertThat(recent).hasSize(2);
        assertThat(recent.get(0).type()).isEqualTo("payment.completed");
        assertThat(recent.get(1).type()).isEqualTo("order.placed");
    }

    @Test
    void record_evictsOldestBeyondCapacity() {
        NotificationStore store = new NotificationStore();
        for (int i = 0; i < 600; i++) {
            store.record(Notification.of("order.placed", "order-" + i, "msg"));
        }

        List<Notification> recent = store.recent();

        assertThat(recent).hasSize(500);
        assertThat(recent.get(0).aggregateId()).isEqualTo("order-599");
    }
}
