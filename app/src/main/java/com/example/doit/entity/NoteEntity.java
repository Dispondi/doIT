package com.example.doit.entity;

public class NoteEntity {
    public static final String DEFAULT_NAME = "Без названия";
    public static final String DEFAULT_CONTENT = "Текст";

    private String name;
    private String content;

    public NoteEntity(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
