package com.tesda.banking.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tesda.banking.transaction.TransactionType;

public record TransactionResponse(
		Long id,
		String accountNumber,
		TransactionType type,
		BigDecimal amount,
		BigDecimal balanceAfter,
		String referenceNumber,
		String remarks,
		LocalDateTime createdAt) {
}
