package com.corefit.controller.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("")
    public ResponseEntity<GeneralResponse<?>> getAllNotifications(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        try {
            GeneralResponse<?> response = notificationService.getNotifications(page, size, request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<GeneralResponse<?>> getUnreadNotificationsCount(HttpServletRequest request) {
        try {
            GeneralResponse<?> response = notificationService.getUnreadCount(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("")
    public ResponseEntity<GeneralResponse<?>> deleteAllNotifications(HttpServletRequest request) {
        try {
            GeneralResponse<?> response = notificationService.deleteAllNotifications(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<?>> deleteAllNotificationById(HttpServletRequest request, @PathVariable Long id) {
        try {
            GeneralResponse<?> response = notificationService.deleteById(request, id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
