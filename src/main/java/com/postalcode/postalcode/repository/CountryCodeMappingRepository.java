package com.postalcode.postalcode.repository;

import com.postalcode.postalcode.model.CountryCodeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryCodeMappingRepository extends JpaRepository<CountryCodeMapping, String> {
}
