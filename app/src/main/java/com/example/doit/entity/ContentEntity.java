package com.example.doit.entity;

import com.google.firebase.firestore.DocumentSnapshot;

public class ContentEntity {

    public static final String DEFAULT_CONTENT = "Здесь пока ничего нет";

    private String content;

    public ContentEntity(String content) {
        this.content = content;
    }

    public ContentEntity(DocumentSnapshot content) {
        this.content = content.getString("content");
    }

    public ContentEntity() {}

    public String getContent() {
        return content;
    }

    public static ContentEntity createDefaultContent() {
        return new ContentEntity(DEFAULT_CONTENT);
    }
}
