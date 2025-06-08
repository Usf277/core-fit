package com.corefit.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
class TestController {
    @Autowired
    private TestService testService;

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

    ///  Pass Controller
    @GetMapping("/pass")
    public ResponseEntity<Integer> generatePass() {
        return ResponseEntity.ok(testService.generatePass());
    }

    @PostMapping("/pass")
    public ResponseEntity<Boolean> checkPass(@RequestBody Password password) {
        return ResponseEntity.ok(testService.checkPass(password));
    }

}
