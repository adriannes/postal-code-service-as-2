package com.postalcode.postalcode.repository;

import com.postalcode.postalcode.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostalCodeRepository extends JpaRepository<Country, String> {

}
