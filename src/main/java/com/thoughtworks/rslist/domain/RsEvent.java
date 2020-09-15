package com.thoughtworks.rslist.domain;

public class RsEvent {

    private String eventName;
    private String keyword;
    private final int id;

    public RsEvent() {
        id = 0;
    }

    public RsEvent(String eventName, String keyword) {
        this.eventName = eventName;
        this.keyword = keyword;
        id = 0;
    }

    public RsEvent(String eventName, String keyword, int id) {
        this.eventName = eventName;
        this.keyword = keyword;
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getId() {
        return id;
    }
}
