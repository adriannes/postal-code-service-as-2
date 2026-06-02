package com.postalcode.postalcode.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "country_code_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryCodeMapping {

    @Id
    @Column(name = "mapped_code", nullable = false, length = 3)
    private String mappedCode;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;
}