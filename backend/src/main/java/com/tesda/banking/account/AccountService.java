package com.tesda.banking.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tesda.banking.common.ConflictException;
import com.tesda.banking.customer.Customer;
import com.tesda.banking.customer.CustomerCredential;
import com.tesda.banking.customer.CustomerCredentialRepository;
import com.tesda.banking.customer.CustomerRepository;
import com.tesda.banking.transaction.BankingTransaction;
import com.tesda.banking.transaction.BankingTransactionRepository;
import com.tesda.banking.transaction.TransactionType;

@Service
public class AccountService {

	private static final BigDecimal ZERO = new BigDecimal("0.00");
	private static final int MAX_ACCOUNT_NUMBER_ATTEMPTS = 10;
	private static final DateTimeFormatter REFERENCE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private final CustomerRepository customerRepository;
	private final CustomerCredentialRepository credentialRepository;
	private final AccountRepository accountRepository;
	private final BankingTransactionRepository transactionRepository;
	private final AccountNumberGenerator accountNumberGenerator;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	public AccountService(CustomerRepository customerRepository,
			CustomerCredentialRepository credentialRepository, AccountRepository accountRepository,
			BankingTransactionRepository transactionRepository, AccountNumberGenerator accountNumberGenerator,
			PasswordEncoder passwordEncoder) {
		this.customerRepository = customerRepository;
		this.credentialRepository = credentialRepository;
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.accountNumberGenerator = accountNumberGenerator;
		this.passwordEncoder = passwordEncoder;
		this.clock = Clock.systemUTC();
	}

	@Transactional
	public AccountResponse createAccount(CreateAccountRequest request) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (customerRepository.existsByEmailIgnoreCase(email)) {
			throw new ConflictException("A customer with this email already exists.");
		}

		LocalDateTime now = LocalDateTime.now(clock);
		BigDecimal openingDeposit = request.openingDeposit() == null
				? ZERO
				: request.openingDeposit().setScale(2, RoundingMode.UNNECESSARY);
		String firstName = request.firstName().trim();
		String lastName = request.lastName().trim();
		String accountHolder = firstName + " " + lastName;

		Customer customer = customerRepository.save(new Customer(firstName, lastName, email,
				request.phoneNumber().trim(), request.dateOfBirth(), now));
		credentialRepository.save(new CustomerCredential(customer, passwordEncoder.encode(request.password()), now));

		Account account = accountRepository.save(new Account(customer, nextUniqueAccountNumber(),
				accountHolder, openingDeposit, now));

		String reference = null;
		if (openingDeposit.signum() > 0) {
			reference = nextReferenceNumber(now);
			transactionRepository.save(new BankingTransaction(account, TransactionType.DEPOSIT,
					openingDeposit, openingDeposit, reference, "Opening deposit", now));
		}

		return new AccountResponse(account.getId(), account.getAccountNumber(), customer.getId(),
				accountHolder, customer.getEmail(), account.getBalance(), account.getCreatedAt(), reference);
	}

	private String nextUniqueAccountNumber() {
		for (int attempt = 0; attempt < MAX_ACCOUNT_NUMBER_ATTEMPTS; attempt++) {
			String candidate = accountNumberGenerator.next();
			if (!accountRepository.existsByAccountNumber(candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException("Unable to generate a unique account number.");
	}

	private String nextReferenceNumber(LocalDateTime now) {
		return "TXN" + now.format(REFERENCE_TIME)
				+ "%06d".formatted(ThreadLocalRandom.current().nextInt(1_000_000));
	}
}
