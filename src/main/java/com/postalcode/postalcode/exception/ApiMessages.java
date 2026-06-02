package com.postalcode.postalcode.exception;

public final class ApiMessages {

    public static final String COUNTRY_CODE_PATTERN = "Country code must contain 2 or 3 letters, or exactly 3 digits";

    private ApiMessages() {
    }

    public static String countryNotFoundInDatabase(String countryCode) {
        return "Country not found in the database for code: " + countryCode;
    }

    public static String countryNotFoundInExternalApi(String countryCode) {
        return "Country not found in REST Countries API for code: " + countryCode;
    }

    public static String failedToCallExternalApi(String countryCode) {
        return "Failed to call REST Countries API for code: " + countryCode;
    }

    public static String noDataReturnedFromExternalApi(String countryCode) {
        return "No data returned from REST Countries API for code: " + countryCode;
    }

    public static String countryNameNullFromExternalApi(String countryCode) {
        return "Country name is null from REST Countries API for code: " + countryCode;
    }

    public static String countryCodeNullFromExternalApi(String countryCode) {
        return "Country code is null from REST Countries API for code: " + countryCode;
    }

    public static String countryAlreadyExistsInDatabase(String countryCode) {
        return "Country already exists in the database for code: " + countryCode;
    }

    public static String countrySuccessfullyAddedToDatabase(String countryCode) {
        return "Country successfully added to the database for code: " + countryCode;
    }
}