package com.tesda.banking.auth;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tesda.banking.account.AccountRepository;
import com.tesda.banking.common.UnauthorizedException;
import com.tesda.banking.customer.Customer;
import com.tesda.banking.customer.CustomerCredential;
import com.tesda.banking.customer.CustomerCredentialRepository;

@Service
public class AuthService {

	private static final String INVALID_CREDENTIALS = "Invalid email or password.";

	private final CustomerCredentialRepository credentialRepository;
	private final AccountRepository accountRepository;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock = Clock.systemUTC();

	public AuthService(CustomerCredentialRepository credentialRepository,
			AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
		this.credentialRepository = credentialRepository;
		this.accountRepository = accountRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(noRollbackFor = UnauthorizedException.class)
	public LoginResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		CustomerCredential credential = credentialRepository.findByCustomerEmailIgnoreCase(email)
				.orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));
		LocalDateTime now = LocalDateTime.now(clock);

		if (credential.getLockedUntil() != null && credential.getLockedUntil().isAfter(now)) {
			throw new UnauthorizedException("Account is temporarily locked. Please try again later.");
		}
		if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
			credential.recordFailedLogin(now);
			throw new UnauthorizedException(INVALID_CREDENTIALS);
		}

		Customer customer = credential.getCustomer();
		if (!"ACTIVE".equals(customer.getStatus())) {
			throw new UnauthorizedException("This account is not active.");
		}

		credential.recordSuccessfulLogin(now);
		String accountNumber = accountRepository.findFirstByCustomerIdOrderByCreatedAtAsc(customer.getId())
				.map(account -> account.getAccountNumber())
				.orElse(null);
		return new LoginResponse(customer.getId(), customer.getFirstName() + " " + customer.getLastName(),
				customer.getEmail(), credential.getRole(), accountNumber);
	}
}
