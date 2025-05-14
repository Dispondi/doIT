package com.example.doit.entity;

import com.google.firebase.firestore.DocumentSnapshot;

public class NoteEntity {
    public static final String DEFAULT_NAME = "Без названия";
    public static final String DEFAULT_CONTENT = "Текст";

    private String name;
    private String content;

    public NoteEntity() {}

    public NoteEntity(DocumentSnapshot note, ContentEntity content) {
        this.name = note.getString("title");
        this.content = content.getContent();
    }

    public String getName() {
        if (name == null) return DEFAULT_NAME;
        else return name;
    }

    public String getContent() {
        if (content == null) return DEFAULT_CONTENT;
        else return content;
    }
}
