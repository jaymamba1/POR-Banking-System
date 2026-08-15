package com.tesda.banking.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankingTransactionRepository extends JpaRepository<BankingTransaction, Long> {

	List<BankingTransaction> findByAccountAccountNumberOrderByCreatedAtDesc(String accountNumber);

	List<BankingTransaction> findAllByOrderByCreatedAtDesc();
}
