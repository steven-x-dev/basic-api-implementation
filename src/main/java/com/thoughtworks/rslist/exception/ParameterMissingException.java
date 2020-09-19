package com.thoughtworks.rslist.exception;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParameterMissingException extends RuntimeException {

    public ParameterMissingException(String ...params) {
        super(generateMessage(params));
    }

    private static String generateMessage(String ...params) {
        Stream<String> filtered = Arrays.stream(params).filter(param -> param != null && param.length() > 0);
        if (filtered.count() == 0) {
            return "params missing";
        } else {
            return String.format("params %s missing", filtered.collect(Collectors.joining(", ")));
        }
    }
}
