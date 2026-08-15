package com.tesda.banking.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tesda.banking.common.BadRequestException;
import com.tesda.banking.common.NotFoundException;
import com.tesda.banking.transaction.BankingTransaction;
import com.tesda.banking.transaction.BankingTransactionRepository;
import com.tesda.banking.transaction.TransactionType;

@Service
public class BankingService {

	private static final DateTimeFormatter REFERENCE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private final AccountRepository accountRepository;
	private final BankingTransactionRepository transactionRepository;
	private final Clock clock = Clock.systemUTC();

	public BankingService(AccountRepository accountRepository,
			BankingTransactionRepository transactionRepository) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}

	@Transactional(readOnly = true)
	public AccountResponse getBalance(String accountNumber) {
		return toAccountResponse(find(accountNumber));
	}

	@Transactional
	public TransactionResponse deposit(String accountNumber, MoneyRequest request) {
		Account account = lock(accountNumber);
		BigDecimal amount = money(request.amount());
		LocalDateTime now = LocalDateTime.now(clock);
		account.deposit(amount, now);
		return saveEntry(account, TransactionType.DEPOSIT, amount, clean(request.remarks()), now);
	}

	@Transactional
	public TransactionResponse withdraw(String accountNumber, MoneyRequest request) {
		Account account = lock(accountNumber);
		BigDecimal amount = money(request.amount());
		ensureFunds(account, amount);
		LocalDateTime now = LocalDateTime.now(clock);
		account.withdraw(amount, now);
		return saveEntry(account, TransactionType.WITHDRAW, amount, clean(request.remarks()), now);
	}

	@Transactional
	public List<TransactionResponse> transfer(String sourceNumber, TransferRequest request) {
		String destinationNumber = request.destinationAccountNumber().trim();
		if (sourceNumber.equals(destinationNumber)) {
			throw new BadRequestException("Source and destination accounts must be different.");
		}

		String firstNumber = sourceNumber.compareTo(destinationNumber) < 0 ? sourceNumber : destinationNumber;
		String secondNumber = sourceNumber.compareTo(destinationNumber) < 0 ? destinationNumber : sourceNumber;
		Account first = lock(firstNumber);
		Account second = lock(secondNumber);
		Account source = sourceNumber.equals(firstNumber) ? first : second;
		Account destination = destinationNumber.equals(firstNumber) ? first : second;
		BigDecimal amount = money(request.amount());
		ensureFunds(source, amount);

		LocalDateTime now = LocalDateTime.now(clock);
		source.withdraw(amount, now);
		destination.deposit(amount, now);
		String remarks = clean(request.remarks());
		TransactionResponse outgoing = saveEntry(source, TransactionType.TRANSFER_OUT, amount,
				"To " + destinationNumber + suffix(remarks), now);
		TransactionResponse incoming = saveEntry(destination, TransactionType.TRANSFER_IN, amount,
				"From " + sourceNumber + suffix(remarks), now);
		return List.of(outgoing, incoming);
	}

	@Transactional(readOnly = true)
	public List<TransactionResponse> history(String accountNumber) {
		find(accountNumber);
		return transactionRepository.findByAccountAccountNumberOrderByCreatedAtDesc(accountNumber)
				.stream().map(this::toTransactionResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<AccountResponse> allAccounts() {
		return accountRepository.findAll().stream().map(this::toAccountResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<TransactionResponse> allTransactions() {
		return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(this::toTransactionResponse).toList();
	}

	private Account find(String number) {
		return accountRepository.findByAccountNumber(number)
				.orElseThrow(() -> new NotFoundException("Account not found."));
	}

	private Account lock(String number) {
		return accountRepository.findForUpdateByAccountNumber(number)
				.orElseThrow(() -> new NotFoundException("Account not found."));
	}

	private void ensureFunds(Account account, BigDecimal amount) {
		if (account.getBalance().compareTo(amount) < 0) {
			throw new BadRequestException("Insufficient account balance.");
		}
	}

	private BigDecimal money(BigDecimal amount) {
		return amount.setScale(2, RoundingMode.UNNECESSARY);
	}

	private TransactionResponse saveEntry(Account account, TransactionType type, BigDecimal amount,
			String remarks, LocalDateTime now) {
		BankingTransaction entry = transactionRepository.save(new BankingTransaction(account, type,
				amount, account.getBalance(), reference(now), remarks, now));
		return toTransactionResponse(entry);
	}

	private String reference(LocalDateTime now) {
		return "TXN" + now.format(REFERENCE_TIME)
				+ "%06d".formatted(ThreadLocalRandom.current().nextInt(1_000_000));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String suffix(String remarks) {
		return remarks == null ? "" : " - " + remarks;
	}

	private AccountResponse toAccountResponse(Account account) {
		return new AccountResponse(account.getId(), account.getAccountNumber(), account.getCustomer().getId(),
				account.getAccountName(), account.getCustomer().getEmail(), account.getBalance(),
				account.getCreatedAt(), null);
	}

	private TransactionResponse toTransactionResponse(BankingTransaction entry) {
		return new TransactionResponse(entry.getId(), entry.getAccount().getAccountNumber(),
				entry.getTransactionType(), entry.getAmount(), entry.getBalanceAfter(),
				entry.getReferenceNumber(), entry.getRemarks(), entry.getCreatedAt());
	}
}
