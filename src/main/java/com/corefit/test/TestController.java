package com.corefit.test;

import com.corefit.entity.auth.User;
import com.corefit.repository.auth.UserRepo;
import com.corefit.service.helper.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
class TestController {
    @Autowired
    private TestService testService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/drop_table")
    public ResponseEntity<String> deleteTable(@RequestParam String name) {
        try {
            String tableName = name.trim();
            if (!testService.tableExists(tableName)) {
                return ResponseEntity.badRequest().body("Table '" + tableName + "' does not exist in the database.");
            }

            if (!testService.isTableDeletionAllowed(tableName)) {
                return ResponseEntity.badRequest().body("Deletion of table '" + tableName + "' is not allowed.");
            }

            testService.deleteTable(tableName);
            return ResponseEntity.ok("Table '" + tableName + "' dropped successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to drop table: " + e.getMessage());
        }
    }

    @GetMapping("/test-fcm")
    public ResponseEntity<?> testFCM(@RequestParam Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        notificationService.pushNotification(user, "🔔 Test Title", "This is a test FCM push.");
        return ResponseEntity.ok("Done");
    }
}
