package com.thoughtworks.rslist.exception;

public class ResourceExistsException extends ResourceException {

    public ResourceExistsException(String resource) {
        super(resource);
    }

    public ResourceExistsException(String resource, String value) {
        super(resource, value);
    }

    @Override
    public String getMessage() {
        return String.format("%s already exists", getResourceIdentifier());
    }

}
