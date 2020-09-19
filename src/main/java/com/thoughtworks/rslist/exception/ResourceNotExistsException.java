package com.thoughtworks.rslist.exception;

public class ResourceNotExistsException extends ResourceException {

    public ResourceNotExistsException(String resource) {
        super(resource);
    }

    public ResourceNotExistsException(String resource, String value) {
        super(resource, value);
    }

    @Override
    public String getMessage() {
        return String.format("%s does not exist", getResourceIdentifier());
    }

}
