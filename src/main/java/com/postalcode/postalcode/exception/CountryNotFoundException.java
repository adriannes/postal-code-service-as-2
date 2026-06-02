package com.postalcode.postalcode.exception;

public class CountryNotFoundException extends RuntimeException {

    public CountryNotFoundException(String countryCode) {
        super(ApiMessages.countryNotFoundInDatabase(countryCode));
    }
}
