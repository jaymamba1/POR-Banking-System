package com.tesda.banking.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
		Long id,
		String accountNumber,
		Long customerId,
		String accountHolder,
		String email,
		BigDecimal balance,
		LocalDateTime createdAt,
		String openingDepositReference) {
}
