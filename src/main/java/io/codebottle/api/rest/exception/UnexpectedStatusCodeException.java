package io.codebottle.api.rest.exception;

import io.codebottle.api.rest.HTTPCodes;

public class UnexpectedStatusCodeException extends RuntimeException {
    public UnexpectedStatusCodeException(int code) {
        super("Unexpected status code: " + HTTPCodes.getString(code));
    }

    public UnexpectedStatusCodeException(int code, String detailMessage) {
        super("Unexpected status code " + HTTPCodes.getString(code) + " with detailed message: " + detailMessage);
    }
}
