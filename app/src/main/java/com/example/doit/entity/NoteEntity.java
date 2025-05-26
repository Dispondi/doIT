package com.example.doit.entity;

import com.google.firebase.Timestamp;

public class NoteEntity {
    public static final String DEFAULT_NAME = "Без названия";
    public static final String DEFAULT_SNIPPET = "";

    public static final String CREATED_AT_FIELD = "createdAt";
    public static final String LAST_UPDATE_FIELD = "last_update";
    public static final String SNIPPET_FIELD = "snippet";
    public static final String TITLE_FIELD = "title";

    public static final int SNIPPET_LENGHT = 20;

    private String title;
    private String snippet;
    private Timestamp createdAt;
    private Timestamp lastUpdate;

    public NoteEntity() {}

    public NoteEntity(String title, String snippet, Timestamp createdAt, Timestamp lastUpdate) {
        this.title = title;
        this.snippet = snippet;
        this.createdAt = createdAt;
        this.lastUpdate = lastUpdate;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public static NoteEntity createDefaultNote() {
        return new NoteEntity(DEFAULT_NAME, DEFAULT_SNIPPET, Timestamp.now(), Timestamp.now());
    }
}
