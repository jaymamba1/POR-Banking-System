package com.tesda.banking.auth;

public record LoginResponse(
		Long customerId,
		String fullName,
		String email,
		String role,
		String accountNumber) {
}
