package com.example.doit.entity;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class UserEntity {

    public static final String USER = "user";
    public static final String ADMIN = "admin";
    public static final String RUS_LANG = "rus";
    public static final String DARK_THEME = "dark";

    private Timestamp createdAt;
    private String role;
    private Map<String, Object> settings;

    public UserEntity() {}

    public UserEntity(Timestamp createdAt, String role, String language, Boolean notificationsEnabled, String theme) {
        this.createdAt = createdAt;
        this.role = role;
        this.settings = new HashMap<>();
        settings.put("language", language);
        settings.put("notificationsEnabled", notificationsEnabled);
        settings.put("theme", theme);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getRole() {
        return role;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public static UserEntity createDefaultUser() {
        return new UserEntity(Timestamp.now(), USER, RUS_LANG, true, DARK_THEME);
    }
}
