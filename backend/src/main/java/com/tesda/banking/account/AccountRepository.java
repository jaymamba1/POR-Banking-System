package com.tesda.banking.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {

	boolean existsByAccountNumber(String accountNumber);

	Optional<Account> findFirstByCustomerIdOrderByCreatedAtAsc(Long customerId);

	Optional<Account> findByAccountNumber(String accountNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Account> findForUpdateByAccountNumber(String accountNumber);
}
