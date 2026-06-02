package com.postalcode.postalcode.service;

import com.postalcode.postalcode.exception.CountryNotFoundException;
import com.postalcode.postalcode.model.CountryCodeMapping;
import com.postalcode.postalcode.model.CountryResponse;
import com.postalcode.postalcode.repository.CountryCodeMappingRepository;
import com.postalcode.postalcode.repository.PostalCodeRepository;

import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class PostalCodeValidationService {

    private final PostalCodeRepository postalCodeRepository;
    private final CountryCodeMappingRepository countryCodeMappingRepository;

    public PostalCodeValidationService(
            PostalCodeRepository postalCodeRepository,
            CountryCodeMappingRepository countryCodeMappingRepository) {
        this.postalCodeRepository = postalCodeRepository;
        this.countryCodeMappingRepository = countryCodeMappingRepository;
    }

    public CountryResponse getByCountryCode(String inputCountryCode) {
        String normalizedCode = inputCountryCode.trim().toUpperCase(Locale.ROOT);
        String repositoryLookupCode = countryCodeMappingRepository.findById(normalizedCode)
                .map(CountryCodeMapping::getCountryCode)
                .orElse(normalizedCode);

        return postalCodeRepository.findById(repositoryLookupCode)
                .map(CountryResponse::from)
                .orElseThrow(() -> new CountryNotFoundException(normalizedCode));
    }
}
