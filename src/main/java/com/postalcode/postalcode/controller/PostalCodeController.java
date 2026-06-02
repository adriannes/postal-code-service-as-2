package com.postalcode.postalcode.controller;

import com.postalcode.postalcode.model.CountryResponse;
import com.postalcode.postalcode.model.ImportCountryResult;
import com.postalcode.postalcode.service.PostalCodeImportService;
import com.postalcode.postalcode.service.PostalCodeValidationService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.postalcode.postalcode.exception.ApiMessages.COUNTRY_CODE_PATTERN;

@RestController
@Validated
@RequestMapping("/api/countries")
public class PostalCodeController {
    private final PostalCodeValidationService postalCodeValidationService;
    private final PostalCodeImportService postalCodeImportService;

    public PostalCodeController(PostalCodeValidationService postalCodeValidationService, PostalCodeImportService postalCodeImportService) {
        this.postalCodeValidationService = postalCodeValidationService;
        this.postalCodeImportService = postalCodeImportService;
    }

    @GetMapping("/{countryCode}")
    public ResponseEntity<CountryResponse> getByCountryCode(
            @PathVariable 
            @Pattern(regexp = "^(?:[A-Za-z]{2,3}|\\d{3})$", message = COUNTRY_CODE_PATTERN)
            String countryCode) {
        CountryResponse response = postalCodeValidationService.getByCountryCode(countryCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add/{countryCode}")
    public ResponseEntity<ImportCountryResult> createByCountryCode(
            @PathVariable
            @Pattern(regexp = "^(?:[A-Za-z]{2,3}|\\d{3})$", message = COUNTRY_CODE_PATTERN)
            String countryCode) {
        ImportCountryResult result = postalCodeImportService.createByCountryCode(countryCode);
        if (!result.created()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}
