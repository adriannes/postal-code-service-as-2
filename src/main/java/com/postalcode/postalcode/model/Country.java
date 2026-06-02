package com.postalcode.postalcode.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "countries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Country {

    @Id
    @Column(name = "country_code", length = 2)
    private String countryCode;

    @NotBlank
    @Column(name = "country_name", nullable = false)
    private String countryName;

    @Column(name = "postal_code_format")
    private String postalCodeFormat;

    @Column(name = "postal_code_regex", length = 500)
    private String postalCodeRegex;
}
