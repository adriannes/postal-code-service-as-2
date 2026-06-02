package com.postalcode.postalcode.service;

import com.postalcode.postalcode.exception.CountryNotFoundException;
import com.postalcode.postalcode.model.Country;
import com.postalcode.postalcode.model.CountryCodeMapping;
import com.postalcode.postalcode.model.CountryResponse;
import com.postalcode.postalcode.repository.CountryCodeMappingRepository;
import com.postalcode.postalcode.repository.PostalCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.postalcode.postalcode.exception.ApiMessages.countryNotFoundInDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostalCodeValidationServiceTest {

    @Mock
    private PostalCodeRepository postalCodeRepository;

    @Mock
    private CountryCodeMappingRepository countryCodeMappingRepository;

    private PostalCodeValidationService postalCodeValidationService;

    @BeforeEach
    void setUp() {
        postalCodeValidationService = new PostalCodeValidationService(
                postalCodeRepository,
                countryCodeMappingRepository
        );
        lenient().when(countryCodeMappingRepository.findById(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void shouldReturnCountryResponseWhenCountryExists() {
        Country country = new Country("NL", "Netherlands", "#### @@", "^(\\d{4}[A-Z]{2})$");
        when(postalCodeRepository.findById("NL")).thenReturn(Optional.of(country));

        CountryResponse result = postalCodeValidationService.getByCountryCode("NL");

        assertEquals("NL", result.countryCode());
        assertEquals("Netherlands", result.countryName());
        assertEquals("#### @@", result.postalCodeFormat());
        assertEquals("^(\\d{4}[A-Z]{2})$", result.postalCodeRegex());
    }

    @Test
    void shouldThrowCountryNotFoundExceptionWhenCountryDoesNotExist() {
        when(postalCodeRepository.findById(anyString())).thenReturn(Optional.empty());

        CountryNotFoundException ex = assertThrows(
                CountryNotFoundException.class,
                () -> postalCodeValidationService.getByCountryCode("UK")
        );

        assertEquals(countryNotFoundInDatabase("UK"), ex.getMessage());
    }

    @Test
    void shouldResolveMappedCodeBeforeRepositoryLookup() {
        Country country = new Country("NL", "Netherlands", "#### @@", "^(\\d{4}[A-Z]{2})$");
        when(countryCodeMappingRepository.findById("NLD"))
                .thenReturn(Optional.of(new CountryCodeMapping("NLD", "NL")));
        when(postalCodeRepository.findById("NL")).thenReturn(Optional.of(country));

        CountryResponse result = postalCodeValidationService.getByCountryCode("nld");

        assertEquals("NL", result.countryCode());
        verify(countryCodeMappingRepository).findById("NLD");
        verify(postalCodeRepository).findById("NL");
    }

    @Test
    void shouldThrowCountryNotFoundExceptionWhenMappedCountryDoesNotExist() {
        when(countryCodeMappingRepository.findById("NLD"))
                .thenReturn(Optional.of(new CountryCodeMapping("NLD", "NL")));
        when(postalCodeRepository.findById("NL")).thenReturn(Optional.empty());

        CountryNotFoundException ex = assertThrows(
                CountryNotFoundException.class,
                () -> postalCodeValidationService.getByCountryCode("nld")
        );

        assertEquals(countryNotFoundInDatabase("NLD"), ex.getMessage());
        verify(countryCodeMappingRepository).findById("NLD");
        verify(postalCodeRepository).findById("NL");
    }
}
