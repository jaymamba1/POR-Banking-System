package com.tesda.banking.account;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;
	private final BankingService bankingService;

	public AccountController(AccountService accountService, BankingService bankingService) {
		this.accountService = accountService;
		this.bankingService = bankingService;
	}

	@PostMapping
	public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
		AccountResponse account = accountService.createAccount(request);
		return ResponseEntity.created(URI.create("/api/accounts/" + account.id())).body(account);
	}

	@GetMapping("/{accountNumber}/balance")
	public AccountResponse balance(@PathVariable String accountNumber) {
		return bankingService.getBalance(accountNumber);
	}

	@PostMapping("/{accountNumber}/deposit")
	public TransactionResponse deposit(@PathVariable String accountNumber,
			@Valid @RequestBody MoneyRequest request) {
		return bankingService.deposit(accountNumber, request);
	}

	@PostMapping("/{accountNumber}/withdraw")
	public TransactionResponse withdraw(@PathVariable String accountNumber,
			@Valid @RequestBody MoneyRequest request) {
		return bankingService.withdraw(accountNumber, request);
	}

	@PostMapping("/{accountNumber}/transfer")
	public List<TransactionResponse> transfer(@PathVariable String accountNumber,
			@Valid @RequestBody TransferRequest request) {
		return bankingService.transfer(accountNumber, request);
	}

	@GetMapping("/{accountNumber}/transactions")
	public List<TransactionResponse> history(@PathVariable String accountNumber) {
		return bankingService.history(accountNumber);
	}

}
