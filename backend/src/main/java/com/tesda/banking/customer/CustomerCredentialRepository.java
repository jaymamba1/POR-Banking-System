package com.tesda.banking.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerCredentialRepository extends JpaRepository<CustomerCredential, Long> {

	Optional<CustomerCredential> findByCustomerEmailIgnoreCase(String email);
}
