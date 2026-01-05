package com.rafhi.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class StorageInitializer {

    @ConfigProperty(name = "history.storage.path")
    String historyStoragePath;

    @ConfigProperty(name = "template.upload.path")
    String templateUploadPath;

    @PostConstruct
    void initStorage() {
        try {
            Files.createDirectories(Paths.get(historyStoragePath));
            Files.createDirectories(Paths.get(templateUploadPath));
        } catch (Exception e) {
            // FAIL FAST: kalau folder tidak bisa dibuat, aplikasi lebih baik tidak jalan
            throw new RuntimeException("Failed to initialize storage directories", e);
        }
    }
}
