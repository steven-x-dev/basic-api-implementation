package com.thoughtworks.rslist.exception;

public class Err {

    private String error;

    public Err() { }

    public Err(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
