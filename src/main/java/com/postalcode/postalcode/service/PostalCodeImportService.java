package com.postalcode.postalcode.service;

import com.postalcode.postalcode.exception.ExternalApiException;
import com.postalcode.postalcode.exception.ExternalCountryNotFoundException;
import com.postalcode.postalcode.exception.ApiMessages;
import com.postalcode.postalcode.model.Country;
import com.postalcode.postalcode.model.CountryCodeMapping;
import com.postalcode.postalcode.model.CountryResponse;
import com.postalcode.postalcode.model.ImportCountryResult;
import com.postalcode.postalcode.model.RestCountriesResponse;
import com.postalcode.postalcode.repository.CountryCodeMappingRepository;
import com.postalcode.postalcode.repository.PostalCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class PostalCodeImportService {

    private final String restCountriesApi;
    private final RestTemplate restTemplate;
    private final PostalCodeRepository postalCodeRepository;
    private final CountryCodeMappingRepository countryCodeMappingRepository;

    public PostalCodeImportService(
            @Value("${restcountries.api.base-url}") String restCountriesApi,
            RestTemplate restTemplate,
            PostalCodeRepository postalCodeRepository,
            CountryCodeMappingRepository countryCodeMappingRepository) {
        this.restCountriesApi = restCountriesApi;
        this.restTemplate = restTemplate;
        this.postalCodeRepository = postalCodeRepository;
        this.countryCodeMappingRepository = countryCodeMappingRepository;
    }

    public ImportCountryResult createByCountryCode(String inputCountryCode) {
        String normalizedCode = inputCountryCode.trim().toUpperCase(Locale.ROOT);
        try {
            String repositoryLookupCode = countryCodeMappingRepository.findById(normalizedCode)
                    .map(CountryCodeMapping::getCountryCode)
                    .orElse(normalizedCode);

            Optional<Country> existingCountry = postalCodeRepository.findById(repositoryLookupCode);
            if (existingCountry.isPresent()) {
                return new ImportCountryResult(
                    CountryResponse.from(existingCountry.get()),
                    false,
                    ApiMessages.countryAlreadyExistsInDatabase(existingCountry.get().getCountryCode())
                );
            }

            String url = restCountriesApi + normalizedCode;

            RestCountriesResponse[] response = restTemplate.getForObject(url, RestCountriesResponse[].class);

            RestCountriesResponse newCountryData = getCountryData(response, inputCountryCode);
            Country country = getCountry( inputCountryCode, newCountryData);

            Country savedCountry = postalCodeRepository.save(country);
            saveCountryCodeMappings(newCountryData, savedCountry.getCountryCode());

            return new ImportCountryResult(
                    CountryResponse.from(savedCountry),
                    true,
                    ApiMessages.countrySuccessfullyAddedToDatabase(savedCountry.getCountryCode())
            );

        } catch (HttpClientErrorException.NotFound e) {
            throw new ExternalCountryNotFoundException(ApiMessages.countryNotFoundInExternalApi(inputCountryCode), e);
        } catch (RestClientException e) {
            throw new ExternalApiException(ApiMessages.failedToCallExternalApi(inputCountryCode), e);
        }
    }

    private void saveCountryCodeMappings(RestCountriesResponse countryData, String countryCode) {
        Stream.of(
                countryData.getCountryCode(),
                countryData.getCcn3(),
                countryData.getCca3(),
                countryData.getCioc()
        )
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .map(value -> new CountryCodeMapping(value, countryCode))
                .forEach(countryCodeMappingRepository::save);
    }

    private static RestCountriesResponse getCountryData(RestCountriesResponse[] response, String inputCountryCode) {
        if (response == null || response.length == 0) {
            throw new ExternalApiException(ApiMessages.noDataReturnedFromExternalApi(inputCountryCode));
        }

        return response[0];
    }

    private static Country getCountry(String inputCountryCode, RestCountriesResponse newCountryData) throws ExternalApiException {

        if (newCountryData.getCountryName() == null) {
            throw new ExternalApiException(ApiMessages.countryNameNullFromExternalApi(inputCountryCode));
        }

        if (newCountryData.getCountryCode() == null) {
            throw new ExternalApiException(ApiMessages.countryCodeNullFromExternalApi(inputCountryCode));
        }

        return new Country(
                newCountryData.getCountryCode(),
                newCountryData.getCountryName(),
                newCountryData.getPostalCodeFormat(),
                newCountryData.getPostalCodeRegex()
        );
    }
}