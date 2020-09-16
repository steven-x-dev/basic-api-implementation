package com.thoughtworks.rslist.exception;

public class UserNameOccupiedException extends RuntimeException {

    public UserNameOccupiedException() {}

    public UserNameOccupiedException(String message) {
        super(message);
    }

}
