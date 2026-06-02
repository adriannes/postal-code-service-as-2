package com.postalcode.postalcode.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RestCountriesResponse(
    @JsonProperty("cca2")
    String countryCode,
    @JsonProperty("ccn3")
    String ccn3,
    @JsonProperty("cca3")
    String cca3,
    @JsonProperty("cioc")
    String cioc,
        @JsonProperty("name")
        NameInfo name,
        @JsonProperty("postalCode")
        PostalCodeInfo postalCode
) {
    public record NameInfo(
            @JsonProperty("common")
            String common
    ) {
    }

    public record PostalCodeInfo(
            @JsonProperty("format")
            String format,
            @JsonProperty("regex")
            String regex
    ) {
    }

    public String getCountryName() {
        return name != null ? name.common : null;
    }

    public String getPostalCodeFormat() {
        return postalCode != null ? postalCode.format : null;
    }

    public String getPostalCodeRegex() {
        return postalCode != null ? postalCode.regex : null;
    }

    public String getCountryCode() {
        return countryCode != null ? countryCode.trim().toUpperCase(Locale.ROOT) : null;
    }

    public String getCcn3() {
        return ccn3 != null ? ccn3.trim() : null;
    }

    public String getCca3() {
        return cca3 != null ? cca3.trim().toUpperCase(Locale.ROOT) : null;
    }

    public String getCioc() {
        return cioc != null ? cioc.trim().toUpperCase(Locale.ROOT) : null;
    }
}