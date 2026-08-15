package com.tesda.banking.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.tesda.banking.customer.CustomerRepository;
import com.tesda.banking.customer.CustomerCredentialRepository;
import com.tesda.banking.transaction.BankingTransactionRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private BankingTransactionRepository transactionRepository;

	@Autowired
	private CustomerCredentialRepository credentialRepository;

	@Autowired
	private BankingService bankingService;

	@Test
	void registersCustomerAndCreatesAccountWithOpeningDeposit() throws Exception {
		mockMvc.perform(post("/api/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validRequest("customer@example.com")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/accounts/")))
				.andExpect(jsonPath("$.accountNumber").value(org.hamcrest.Matchers.matchesPattern("ACC-[0-9]{10}")))
				.andExpect(jsonPath("$.accountHolder").value("Juan Dela Cruz"))
				.andExpect(jsonPath("$.balance").value(5000.00))
				.andExpect(jsonPath("$.openingDepositReference").isNotEmpty());

		assertThat(customerRepository.count()).isEqualTo(1);
		assertThat(accountRepository.count()).isEqualTo(1);
		assertThat(transactionRepository.count()).isEqualTo(1);
		assertThat(credentialRepository.count()).isEqualTo(1);
	}

	@Test
	void rejectsInvalidRequest() throws Exception {
		mockMvc.perform(post("/api/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "firstName": "",
						  "lastName": "",
						  "email": "invalid",
						  "phoneNumber": "bad",
						  "dateOfBirth": "2099-01-01",
						  "password": "short",
						  "acceptedTerms": false,
						  "openingDeposit": -1
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.firstName").exists())
				.andExpect(jsonPath("$.fieldErrors.lastName").exists())
				.andExpect(jsonPath("$.fieldErrors.email").exists())
				.andExpect(jsonPath("$.fieldErrors.openingDeposit").exists());
	}

	@Test
	void rejectsDuplicateCustomerEmail() throws Exception {
		String request = validRequest("duplicate@example.com");
		mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(request))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(request))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("A customer with this email already exists."));
	}

	@Test
	void registeredCustomerCanLogin() throws Exception {
		mockMvc.perform(post("/api/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validRequest("login@example.com")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"login@example.com","password":"SecurePass123!"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fullName").value("Juan Dela Cruz"))
				.andExpect(jsonPath("$.role").value("CUSTOMER"))
				.andExpect(jsonPath("$.accountNumber").value(org.hamcrest.Matchers.matchesPattern("ACC-[0-9]{10}")));
	}

	@Test
	void rejectsIncorrectPassword() throws Exception {
		mockMvc.perform(post("/api/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validRequest("wrong-password@example.com")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"wrong-password@example.com","password":"Incorrect123!"}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid email or password."));
	}

	@Test
	void depositWithdrawAndTransferKeepBalancesAndLedgerConsistent() throws Exception {
		mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
				.content(validRequest("sender@example.com"))).andExpect(status().isCreated());
		mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
				.content(validRequest("receiver@example.com"))).andExpect(status().isCreated());

		var accounts = accountRepository.findAll();
		String sender = accounts.get(0).getAccountNumber();
		String receiver = accounts.get(1).getAccountNumber();

		bankingService.deposit(sender, new MoneyRequest(new java.math.BigDecimal("1000.00"), "Cash deposit"));
		bankingService.withdraw(sender, new MoneyRequest(new java.math.BigDecimal("200.00"), "ATM"));
		bankingService.transfer(sender,
				new TransferRequest(receiver, new java.math.BigDecimal("300.00"), "Payment"));

		assertThat(bankingService.getBalance(sender).balance()).isEqualByComparingTo("5500.00");
		assertThat(bankingService.getBalance(receiver).balance()).isEqualByComparingTo("5300.00");
		assertThat(bankingService.history(sender)).hasSize(4);
		assertThat(bankingService.history(receiver)).hasSize(2);
		assertThat(bankingService.allAccounts()).hasSize(2);
		assertThat(bankingService.allTransactions()).hasSize(6);
	}

	private String validRequest(String email) {
		return """
				{
				  "firstName": "Juan",
				  "lastName": "Dela Cruz",
				  "email": "%s",
				  "phoneNumber": "+63 917 123 4567",
				  "dateOfBirth": "1995-06-15",
				  "password": "SecurePass123!",
				  "acceptedTerms": true,
				  "openingDeposit": 5000.00
				}
				""".formatted(email);
	}
}
