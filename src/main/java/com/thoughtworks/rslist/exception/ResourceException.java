package com.thoughtworks.rslist.exception;

public abstract class ResourceException extends RuntimeException {

    protected String resource;
    protected String value;

    protected ResourceException(String resource) {
        this.resource = resource;
    }

    protected ResourceException(String resource, String value) {
        this.resource = resource;
        this.value = value;
    }

    protected String getResourceIdentifier() {
        if (value == null) {
            return resource;
        } else {
            return String.format("%s %s", resource, value);
        }
    }

    public abstract String getMessage();

}
