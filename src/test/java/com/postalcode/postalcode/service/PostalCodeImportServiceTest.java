package com.postalcode.postalcode.service;

import com.postalcode.postalcode.exception.ExternalApiException;
import com.postalcode.postalcode.exception.ExternalCountryNotFoundException;
import com.postalcode.postalcode.model.Country;
import com.postalcode.postalcode.model.CountryCodeMapping;
import com.postalcode.postalcode.model.ImportCountryResult;
import com.postalcode.postalcode.model.RestCountriesResponse;
import com.postalcode.postalcode.repository.CountryCodeMappingRepository;
import com.postalcode.postalcode.repository.PostalCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.postalcode.postalcode.exception.ApiMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostalCodeImportServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PostalCodeRepository postalCodeRepository;

    @Mock
    private CountryCodeMappingRepository countryCodeMappingRepository;

    private PostalCodeImportService postalCodeImportService;

    @BeforeEach
    void setUp() {
        postalCodeImportService = new PostalCodeImportService(
                "https://restcountries.com/v3.1/alpha/",
                restTemplate,
                postalCodeRepository,
                countryCodeMappingRepository
        );
        Mockito.lenient().when(countryCodeMappingRepository.findById(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void shouldReturnExistingCountryWithoutCallingExternalApi() {
        Country existing = new Country("NL", "Netherlands", "#### @@", "^(\\d{4}[A-Z]{2})$");
        when(postalCodeRepository.findById("NL")).thenReturn(Optional.of(existing));

        ImportCountryResult result = postalCodeImportService.createByCountryCode("nl");

        assertEquals("NL", result.response().countryCode());
        assertEquals("Netherlands", result.response().countryName());
        assertEquals(countryAlreadyExistsInDatabase("NL"), result.message());
        assertFalse(result.created());
        verify(countryCodeMappingRepository).findById("NL");
        verifyNoInteractions(restTemplate);
        verify(postalCodeRepository, never()).save(any(Country.class));
        }

        @Test
        void shouldResolveMappedCodeBeforeCheckingExistingCountry() {
                Country existing = new Country("NL", "Netherlands", "#### @@", "^(\\d{4}[A-Z]{2})$");
                when(countryCodeMappingRepository.findById("NLD"))
                                .thenReturn(Optional.of(new CountryCodeMapping("NLD", "NL")));
                when(postalCodeRepository.findById("NL")).thenReturn(Optional.of(existing));

                ImportCountryResult result = postalCodeImportService.createByCountryCode("nld");

                assertEquals("NL", result.response().countryCode());
                assertEquals(countryAlreadyExistsInDatabase("NL"), result.message());
                assertFalse(result.created());
                verify(countryCodeMappingRepository).findById("NLD");
                verify(postalCodeRepository).findById("NL");
                verifyNoInteractions(restTemplate);
                verify(postalCodeRepository, never()).save(any(Country.class));
    }

    @Test
    void shouldImportAndSaveCountryWhenNotFoundInDatabase() {
        when(postalCodeRepository.findById("NL")).thenReturn(Optional.empty());

        RestCountriesResponse response = new RestCountriesResponse(
                "nl",
                "528",
                "NLD",
                "NED",
                new RestCountriesResponse.NameInfo("Netherlands"),
                new RestCountriesResponse.PostalCodeInfo("#### @@", "^(\\d{4}[A-Z]{2})$")
        );

        Country savedCountry = new Country("NL", "Netherlands", "#### @@", "^(\\d{4}[A-Z]{2})$");

        when(restTemplate.getForObject("https://restcountries.com/v3.1/alpha/NL", RestCountriesResponse[].class))
                .thenReturn(new RestCountriesResponse[]{response});
        when(postalCodeRepository.save(any(Country.class))).thenReturn(savedCountry);

        ImportCountryResult result = postalCodeImportService.createByCountryCode(" nl ");

        assertEquals("NL", result.response().countryCode());
        assertEquals("Netherlands", result.response().countryName());
        assertEquals(countrySuccessfullyAddedToDatabase("NL"), result.message());
        assertTrue(result.created());
        verify(postalCodeRepository).findById("NL");
        verify(restTemplate).getForObject("https://restcountries.com/v3.1/alpha/NL", RestCountriesResponse[].class);
        verify(postalCodeRepository).save(any(Country.class));
        verify(countryCodeMappingRepository, times(4)).save(any(CountryCodeMapping.class));
        verify(countryCodeMappingRepository).save(new CountryCodeMapping("NL", "NL"));
        verify(countryCodeMappingRepository).save(new CountryCodeMapping("528", "NL"));
        verify(countryCodeMappingRepository).save(new CountryCodeMapping("NLD", "NL"));
        verify(countryCodeMappingRepository).save(new CountryCodeMapping("NED", "NL"));
    }

        @Test
        void shouldSaveOnlyDistinctAndNonBlankCountryCodeMappings() {
                when(postalCodeRepository.findById("NL")).thenReturn(Optional.empty());

                RestCountriesResponse response = new RestCountriesResponse(
                                "NL",
                                " ",
                                "NLD",
                                "NLD",
                                new RestCountriesResponse.NameInfo("Netherlands"),
                                new RestCountriesResponse.PostalCodeInfo("#### @@", "^(\\d{4}[A-Z]{2})$")
                );

                Country savedCountry = new Country("NL", "Netherlands", "#### @@", "^(\\d{4}[A-Z]{2})$");

                when(restTemplate.getForObject("https://restcountries.com/v3.1/alpha/NL", RestCountriesResponse[].class))
                                .thenReturn(new RestCountriesResponse[]{response});
                when(postalCodeRepository.save(any(Country.class))).thenReturn(savedCountry);

                postalCodeImportService.createByCountryCode("nl");

                verify(countryCodeMappingRepository, times(2)).save(any(CountryCodeMapping.class));
                verify(countryCodeMappingRepository).save(new CountryCodeMapping("NL", "NL"));
                verify(countryCodeMappingRepository).save(new CountryCodeMapping("NLD", "NL"));
        }

    @Test
    void shouldThrowExternalCountryNotFoundExceptionWhenApiReturns404() {
        when(postalCodeRepository.findById("UK")).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(RestCountriesResponse[].class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8
                ));

        ExternalCountryNotFoundException ex = assertThrows(
                ExternalCountryNotFoundException.class,
                () -> postalCodeImportService.createByCountryCode("uk")
        );

        assertEquals(countryNotFoundInExternalApi("uk"), ex.getMessage());
        verify(countryCodeMappingRepository).findById("UK");
    }

    @Test
    void shouldCallExternalApiWhenMappedCountryIsMissingInDatabase() {
        when(countryCodeMappingRepository.findById("NLD"))
                .thenReturn(Optional.of(new CountryCodeMapping("NLD", "NL")));
        when(postalCodeRepository.findById("NL")).thenReturn(Optional.empty());
        when(restTemplate.getForObject("https://restcountries.com/v3.1/alpha/NLD", RestCountriesResponse[].class))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8
                ));

        ExternalCountryNotFoundException ex = assertThrows(
                ExternalCountryNotFoundException.class,
                () -> postalCodeImportService.createByCountryCode("nld")
        );

        assertEquals(countryNotFoundInExternalApi("nld"), ex.getMessage());
        verify(countryCodeMappingRepository).findById("NLD");
        verify(postalCodeRepository).findById("NL");
        verify(restTemplate).getForObject("https://restcountries.com/v3.1/alpha/NLD", RestCountriesResponse[].class);
    }

    @Test
    void shouldThrowExternalApiExceptionWhenApiCallFails() {
        when(postalCodeRepository.findById("NL")).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(RestCountriesResponse[].class)))
                .thenThrow(new RestClientException("Connection reset"));

        ExternalApiException ex = assertThrows(
                ExternalApiException.class,
                () -> postalCodeImportService.createByCountryCode("nl")
        );

        assertEquals(failedToCallExternalApi("nl"), ex.getMessage());
    }

    @Test
    void shouldThrowExternalApiExceptionWhenApiReturnsEmptyPayload() {
        when(postalCodeRepository.findById("NL")).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(RestCountriesResponse[].class)))
                .thenReturn(new RestCountriesResponse[0]);

        ExternalApiException ex = assertThrows(
                ExternalApiException.class,
                () -> postalCodeImportService.createByCountryCode("nl")
        );

        assertEquals(noDataReturnedFromExternalApi("nl"), ex.getMessage());
    }

    @Test
    void shouldThrowExternalApiExceptionWhenCountryCodeIsMissingInApiPayload() {
        when(postalCodeRepository.findById("NL")).thenReturn(Optional.empty());

        RestCountriesResponse response = new RestCountriesResponse(
                null,
                "528",
                "NLD",
                "NED",
                new RestCountriesResponse.NameInfo("Netherlands"),
                new RestCountriesResponse.PostalCodeInfo("#### @@", "^(\\d{4}[A-Z]{2})$")
        );

        when(restTemplate.getForObject(anyString(), eq(RestCountriesResponse[].class)))
                .thenReturn(new RestCountriesResponse[]{response});

        ExternalApiException ex = assertThrows(
                ExternalApiException.class,
                () -> postalCodeImportService.createByCountryCode("nl")
        );

        assertEquals(countryCodeNullFromExternalApi("nl"), ex.getMessage());
    }
}
