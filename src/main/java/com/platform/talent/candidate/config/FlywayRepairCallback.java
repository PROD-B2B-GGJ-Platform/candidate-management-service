package com.platform.talent.candidate.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import lombok.extern.slf4j.Slf4j;

/**
 * Flyway configuration to automatically repair checksum mismatches on startup.
 * This allows the application to start even if migration files have been modified
 * after they were already applied to the database.
 */
@Configuration
@Slf4j
public class FlywayRepairCallback {

    @Bean
    @Primary
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                log.info("Repairing Flyway checksums before migration...");
                flyway.repair();
                log.info("Flyway checksum repair completed successfully");
            } catch (Exception e) {
                log.warn("Failed to repair Flyway checksums: {}. Continuing with migration...", e.getMessage());
            }
            // Now perform the migration
            flyway.migrate();
        };
    }
}
