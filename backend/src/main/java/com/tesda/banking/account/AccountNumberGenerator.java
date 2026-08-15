package com.tesda.banking.account;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class AccountNumberGenerator {

	private static final long ACCOUNT_NUMBER_BOUND = 10_000_000_000L;
	private final SecureRandom secureRandom = new SecureRandom();

	public String next() {
		return "ACC-%010d".formatted(secureRandom.nextLong(ACCOUNT_NUMBER_BOUND));
	}
}
