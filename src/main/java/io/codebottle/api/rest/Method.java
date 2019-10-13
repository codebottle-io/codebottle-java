package io.codebottle.api.rest;

public enum Method {
    GET,

    POST,

    PUT,

    PATCH,

    DELETE,

    HEAD;

    @Override
    public String toString() {
        return name();
    }
}
