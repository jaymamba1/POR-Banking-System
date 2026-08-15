package com.tesda.banking.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tesda.banking.account.AccountResponse;
import com.tesda.banking.account.BankingService;
import com.tesda.banking.account.TransactionResponse;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final BankingService bankingService;

	public AdminController(BankingService bankingService) {
		this.bankingService = bankingService;
	}

	@GetMapping("/accounts")
	public List<AccountResponse> accounts() {
		return bankingService.allAccounts();
	}

	@GetMapping("/transactions")
	public List<TransactionResponse> transactions() {
		return bankingService.allTransactions();
	}
}
