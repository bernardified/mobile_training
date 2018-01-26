package com.shopback.notepad;

class Note {
    enum NoteType {
        NOTES
    }

    private Long id;
    private String title, body;
    private NoteType type;
    private String lastUpdated;

    Note(Long id, String title, String body, NoteType type, String lastUpdated) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.type = type;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    NoteType getType() {
        return type;
    }
}
