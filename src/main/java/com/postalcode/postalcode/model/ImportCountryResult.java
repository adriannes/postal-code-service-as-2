package com.postalcode.postalcode.model;

public record ImportCountryResult(CountryResponse response, boolean created, String message) {
}