package com.example.doit.entity;

import com.google.firebase.firestore.DocumentSnapshot;

public class ContentEntity {
    private String content;

    public ContentEntity(String content) {
        this.content = content;
    }

    public ContentEntity(DocumentSnapshot content) {
        this.content = content.getString("content");
    }

    public ContentEntity() {}

    public String getContent() {
        if (content == null) return "";
        else return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContent(DocumentSnapshot content) {
        this.content = content.getString("content");
    }
}
