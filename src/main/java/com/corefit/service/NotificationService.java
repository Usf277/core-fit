package com.corefit.service;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.NotificationResponse;
import com.corefit.entity.FcmToken;
import com.corefit.entity.Notification;
import com.corefit.entity.User;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.FcmTokenRepo;
import com.corefit.repository.NotificationRepo;
import com.corefit.service.market.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepo notificationRepo;
    @Autowired
    private FcmTokenRepo fcmTokenRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private FCMService fcmService;

    public GeneralResponse<?> getNotifications(Integer page, Integer size, HttpServletRequest request) {
        if (size == null || size <= 0) size = 5;
        if (page == null || page < 1) page = 1;

        User user = authService.extractUserFromRequest(request);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Notification> notifications = notificationRepo.findAllByUserId(user.getId(), pageable);

        notificationRepo.markAllAsReadByUserId(user.getId());

        Page<NotificationResponse> notificationsResponse = notifications.map(this::mapToNotificationResponse);

        Map<String, Object> data = new HashMap<>();
        data.put("notifications", notificationsResponse.getContent());
        data.put("totalPages", notificationsResponse.getTotalPages());
        data.put("totalElements", notificationsResponse.getTotalElements());
        data.put("pageSize", notificationsResponse.getSize());

        return new GeneralResponse<>("Notifications retrieved successfully", data);
    }

    public GeneralResponse<?> getUnreadCount(HttpServletRequest request) {
        User user = authService.extractUserFromRequest(request);
        Integer count = notificationRepo.getAllUserUnreadCount(user.getId());
        return new GeneralResponse<>("Unread notifications count retrieved", count);
    }

    public GeneralResponse<?> deleteAllNotifications(HttpServletRequest request) {
        User user = authService.extractUserFromRequest(request);
        notificationRepo.deleteAllByUserId(user.getId());
        return new GeneralResponse<>("All notifications deleted successfully");
    }

    public GeneralResponse<?> deleteById(HttpServletRequest request, Long id) {
        User user = authService.extractUserFromRequest(request);
        Optional<Notification> notification = notificationRepo.findById(id);

        if (notification.isPresent() && notification.get().getUser().getId().equals(user.getId())) {
            notificationRepo.deleteById(id);
            return new GeneralResponse<>("Notification deleted successfully");
        } else {
            throw new GeneralException("Notification not found or unauthorized");
        }
    }

    @Transactional
    public void pushNotification(User user, String title, String message) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setUser(user);
        notification.setMessage(message);

        FcmToken fcmToken = fcmTokenRepo.findByUserId(user.getId());

        notificationRepo.save(notification);
        fcmService.sendNotification(title, message, fcmToken.getToken());
    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.getIsRead())
                .build();
    }
}
