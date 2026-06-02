package com.postalcode.postalcode.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CountryResponse(
        String countryCode,
        String countryName,
        String postalCodeFormat,
        String postalCodeRegex
) {

    public static CountryResponse from(Country country) {
        return new CountryResponse(
                country.getCountryCode(),
                country.getCountryName(),
                country.getPostalCodeFormat(),
                country.getPostalCodeRegex()
        );
    }
}
