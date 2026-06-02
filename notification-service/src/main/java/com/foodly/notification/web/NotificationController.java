package com.foodly.notification.web;

import com.foodly.common.api.ApiResponse;
import com.foodly.notification.model.Notification;
import com.foodly.notification.store.NotificationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Inspection endpoint for recently delivered notifications. Reached via the gateway as {@code /api/notifications/**}. */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationStore store;

    @GetMapping
    public ApiResponse<List<Notification>> recent() {
        return ApiResponse.ok(store.recent());
    }
}
