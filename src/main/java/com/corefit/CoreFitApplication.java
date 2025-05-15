package com.corefit;

import com.corefit.exceptions.GeneralException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@SpringBootApplication
@EnableJpaAuditing
public class CoreFitApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreFitApplication.class, args);
    }
}

@RestController
class DeleteTableController {
    @Autowired
    private DeleteTableService deleteTableService;

    @PostMapping("/drop_table")
    public ResponseEntity<String> deleteTable(@RequestParam String name) {
        try {
            // Trim the input and ensure it's a single table name
            String tableName = name.trim();

            // First check if the table exists
            if (!deleteTableService.tableExists(tableName)) {
                return ResponseEntity.badRequest().body("Table '" + tableName + "' does not exist in the database.");
            }

            // Validate that this is actually a table we want to allow deleting
            if (!deleteTableService.isTableDeletionAllowed(tableName)) {
                return ResponseEntity.badRequest().body("Deletion of table '" + tableName + "' is not allowed.");
            }

            deleteTableService.deleteTable(tableName);
            return ResponseEntity.ok("Table '" + tableName + "' dropped successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to drop table: " + e.getMessage());
        }
    }
}

@Service
class DeleteTableService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private org.springframework.core.env.Environment env;

    // Pattern to validate table names (alphanumeric plus underscore)
    private static final Pattern VALID_TABLE_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

    // Add a list of tables that are allowed to be deleted
    private static final String[] ALLOWED_TABLES = {
            "example_table",
            "test_data",
            "playgrounds",
            // Add other tables that should be deletable
    };

    public boolean isTableDeletionAllowed(String tableName) {
        // First validate the table name format to prevent SQL injection
        if (!VALID_TABLE_NAME.matcher(tableName).matches()) {
            return false;
        }
        return true;
    }

    public boolean tableExists(String tableName) {
        try {
            // Extract database name from the environment variable or use DATABASE() function
            String dbName = null;

            try {
                // Try to get database name from Spring Environment
                String dbUrl = env.getProperty("spring.datasource.url");
                if (dbUrl != null && dbUrl.contains("/")) {
                    // Extract database name from JDBC URL
                    String[] urlParts = dbUrl.split("/");
                    if (urlParts.length > 3) {
                        dbName = urlParts[3].split("\\?")[0]; // Get database name before parameters
                    }
                }
            } catch (Exception ex) {
                throw new GeneralException("Error" + ex.getMessage());
            }

            // If we couldn't get it from environment, query the database
            if (dbName == null || dbName.isEmpty()) {
                dbName = (String) entityManager.createNativeQuery("SELECT DATABASE()").getSingleResult();
            }

            // Check if the table exists in the INFORMATION_SCHEMA using positional parameters
            Long count = (Long) entityManager.createNativeQuery(
                            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                                    "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?")
                    .setParameter(1, dbName)
                    .setParameter(2, tableName)
                    .getSingleResult();

            return count > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error checking if table exists: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteTable(String tableName) {
        // Double-check that the table name is valid before executing
        if (!isTableDeletionAllowed(tableName)) {
            throw new IllegalArgumentException("Invalid table name or deletion not allowed");
        }

        // Check if the table exists first
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exist in the database");
        }

        try {
            // Use a prepared statement approach for extra safety
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            entityManager.createNativeQuery("DROP TABLE `" + tableName + "`").executeUpdate();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

            // Verify table no longer exists
            if (tableExists(tableName)) {
                throw new RuntimeException("Failed to drop table '" + tableName + "' - table still exists");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error dropping table: " + e.getMessage(), e);
        }
    }
}