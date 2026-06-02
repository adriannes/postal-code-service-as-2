package com.postalcode.postalcode.exception;

public class ExternalCountryNotFoundException extends ExternalApiException {

    public ExternalCountryNotFoundException(String message) {
        super(message);
    }

    public ExternalCountryNotFoundException(String message, Throwable cause) {

        super(message, cause);
    }
}