package com.corefit.test;

import com.corefit.exceptions.GeneralException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Service
class TestService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private org.springframework.core.env.Environment env;
    private static final Pattern VALID_TABLE_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

    private static final String[] ALLOWED_TABLES = {
            "example_table",
            "test_data",
            "playgrounds",
    };

    public boolean isTableDeletionAllowed(String tableName) {
        if (!VALID_TABLE_NAME.matcher(tableName).matches()) {
            return false;
        }
        return true;
    }

    public boolean tableExists(String tableName) {
        try {
            String dbName = null;

            try {
                String dbUrl = env.getProperty("spring.datasource.url");
                if (dbUrl != null && dbUrl.contains("/")) {
                    String[] urlParts = dbUrl.split("/");
                    if (urlParts.length > 3) {
                        dbName = urlParts[3].split("\\?")[0]; // Get database name before parameters
                    }
                }
            } catch (Exception ex) {
                throw new GeneralException("Error" + ex.getMessage());
            }

            if (dbName == null || dbName.isEmpty()) {
                dbName = (String) entityManager.createNativeQuery("SELECT DATABASE()").getSingleResult();
            }

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
        if (!isTableDeletionAllowed(tableName)) {
            throw new IllegalArgumentException("Invalid table name or deletion not allowed");
        }

        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exist in the database");
        }

        try {
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            entityManager.createNativeQuery("DROP TABLE `" + tableName + "`").executeUpdate();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

            if (tableExists(tableName)) {
                throw new RuntimeException("Failed to drop table '" + tableName + "' - table still exists");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error dropping table: " + e.getMessage(), e);
        }
    }

    /// Pass Service
    @Autowired
    private PassRepository passRepository;

    public int generatePass() {
        int randomPass = new Random().nextInt(900_000) + 100_000;
        Pass pass = Pass.builder().pass(randomPass).build();
        passRepository.save(pass);
        return randomPass;
    }

    public String checkPass(Pass pass) {
        Optional<Pass> optionalPass = passRepository.findById(pass.getId());

        if (optionalPass.isEmpty()) {
            return "❌ No record found for the provided ID.";
        }

        Pass savedPass = optionalPass.get();

        if (savedPass.getPass() == pass.getPass()) {
            return "✅ The password you entered is correct.";
        } else {
            return "❌ The password you entered is incorrect. Please try again.";
        }
    }
}
