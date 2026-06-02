package com.postalcode.postalcode.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static com.postalcode.postalcode.exception.ApiMessages.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostalCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnBadRequestWhenCountryCodeDoesntFollowPattern() throws Exception {
        mockMvc.perform(get("/api/countries/  "))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(COUNTRY_CODE_PATTERN));

        mockMvc.perform(get("/api/countries/neld"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(COUNTRY_CODE_PATTERN));

        mockMvc.perform(get("/api/countries/12n"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(COUNTRY_CODE_PATTERN));
    }

    @Test
    void shouldReturnNotFoundWhenCountryNotInDatabase() throws Exception {
        mockMvc.perform(get("/api/countries/uk"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value(countryNotFoundInDatabase("UK")));
    }

    @Test
    void shouldReturnBadRequestWhenImportCountryCodeIsBlank() throws Exception {
        mockMvc.perform(post("/api/countries/add/  "))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(COUNTRY_CODE_PATTERN));

        mockMvc.perform(post("/api/countries/add/neld"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(COUNTRY_CODE_PATTERN));

        mockMvc.perform(post("/api/countries/add/12n"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(COUNTRY_CODE_PATTERN));
    }

    @Test
    void shouldReturnNotFoundWhenImportingUnknownCountryCode() throws Exception {
        mockMvc.perform(post("/api/countries/add/uk"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value(countryNotFoundInExternalApi("uk")));
    }

    @Test
    void shouldImportGbAndThenFindItByCode() throws Exception {
        mockMvc.perform(post("/api/countries/add/gbr"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.created").value(true))
            .andExpect(jsonPath("$.message").value(countrySuccessfullyAddedToDatabase("GB")))
            .andExpect(jsonPath("$.response.countryCode").value("GB"))
            .andExpect(jsonPath("$.response.countryName").value("United Kingdom"))
            .andExpect(jsonPath("$.response.postalCodeFormat").value("@# #@@|@## #@@|@@# #@@|@@## #@@|@#@ #@@|@@#@ #@@|GIR0AA"))
            .andExpect(jsonPath("$.response.postalCodeRegex").value("^(([A-Z]\\d{2}[A-Z]{2})|([A-Z]\\d{3}[A-Z]{2})|([A-Z]{2}\\d{2}[A-Z]{2})|([A-Z]{2}\\d{3}[A-Z]{2})|([A-Z]\\d[A-Z]\\d[A-Z]{2})|([A-Z]{2}\\d[A-Z]\\d[A-Z]{2})|(GIR0AA))$"));

        mockMvc.perform(get("/api/countries/gb"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.countryCode").value("GB"))
            .andExpect(jsonPath("$.countryName").value("United Kingdom"))
            .andExpect(jsonPath("$.postalCodeFormat").value("@# #@@|@## #@@|@@# #@@|@@## #@@|@#@ #@@|@@#@ #@@|GIR0AA"))
            .andExpect(jsonPath("$.postalCodeRegex").value("^(([A-Z]\\d{2}[A-Z]{2})|([A-Z]\\d{3}[A-Z]{2})|([A-Z]{2}\\d{2}[A-Z]{2})|([A-Z]{2}\\d{3}[A-Z]{2})|([A-Z]\\d[A-Z]\\d[A-Z]{2})|([A-Z]{2}\\d[A-Z]\\d[A-Z]{2})|(GIR0AA))$"));
    }

    @Test
    void shouldImportNlAndThenFindItByCodeAndThenImportAgain() throws Exception {
        mockMvc.perform(post("/api/countries/add/nl"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.created").value(true))
            .andExpect(jsonPath("$.message").value(countrySuccessfullyAddedToDatabase("NL")))
            .andExpect(jsonPath("$.response.countryCode").value("NL"))
            .andExpect(jsonPath("$.response.countryName").value("Netherlands"))
            .andExpect(jsonPath("$.response.postalCodeFormat").value("#### @@"))
            .andExpect(jsonPath("$.response.postalCodeRegex").value("^(\\d{4}[A-Z]{2})$"));

        mockMvc.perform(get("/api/countries/nl"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.countryCode").value("NL"))
            .andExpect(jsonPath("$.countryName").value("Netherlands"))
            .andExpect(jsonPath("$.postalCodeFormat").value("#### @@"))
            .andExpect(jsonPath("$.postalCodeRegex").value("^(\\d{4}[A-Z]{2})$"));

        mockMvc.perform(get("/api/countries/nld"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.countryCode").value("NL"))
            .andExpect(jsonPath("$.countryName").value("Netherlands"))
            .andExpect(jsonPath("$.postalCodeFormat").value("#### @@"))
            .andExpect(jsonPath("$.postalCodeRegex").value("^(\\d{4}[A-Z]{2})$"));

        mockMvc.perform(get("/api/countries/ned"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.countryCode").value("NL"))
            .andExpect(jsonPath("$.countryName").value("Netherlands"))
            .andExpect(jsonPath("$.postalCodeFormat").value("#### @@"))
            .andExpect(jsonPath("$.postalCodeRegex").value("^(\\d{4}[A-Z]{2})$"));

        mockMvc.perform(get("/api/countries/528"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.countryCode").value("NL"))
            .andExpect(jsonPath("$.countryName").value("Netherlands"))
            .andExpect(jsonPath("$.postalCodeFormat").value("#### @@"))
            .andExpect(jsonPath("$.postalCodeRegex").value("^(\\d{4}[A-Z]{2})$"));


        mockMvc.perform(post("/api/countries/add/nl"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.created").value(false))
            .andExpect(jsonPath("$.message").value(countryAlreadyExistsInDatabase("NL")))
            .andExpect(jsonPath("$.response.countryCode").value("NL"))
            .andExpect(jsonPath("$.response.countryName").value("Netherlands"))
            .andExpect(jsonPath("$.response.postalCodeFormat").value("#### @@"))
            .andExpect(jsonPath("$.response.postalCodeRegex").value("^(\\d{4}[A-Z]{2})$"));
    }

    @Test
    void shouldImportQaAndThenFindItByCode() throws Exception {
        mockMvc.perform(post("/api/countries/add/634"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.created").value(true))
            .andExpect(jsonPath("$.message").value(countrySuccessfullyAddedToDatabase("QA")))
            .andExpect(jsonPath("$.response.countryCode").value("QA"))
            .andExpect(jsonPath("$.response.countryName").value("Qatar"))
            .andExpect(jsonPath("$.response.postalCodeFormat").doesNotExist())
            .andExpect(jsonPath("$.response.postalCodeRegex").doesNotExist());

        mockMvc.perform(get("/api/countries/qa"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.countryCode").value("QA"))
            .andExpect(jsonPath("$.countryName").value("Qatar"))
            .andExpect(jsonPath("$.postalCodeFormat").doesNotExist())
            .andExpect(jsonPath("$.postalCodeRegex").doesNotExist());
    }
}
