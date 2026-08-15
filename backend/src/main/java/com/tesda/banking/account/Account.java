package com.tesda.banking.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tesda.banking.customer.Customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@Column(name = "account_number", nullable = false, unique = true, length = 20)
	private String accountNumber;

	@Column(name = "account_name", nullable = false, length = 100)
	private String accountName;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal balance;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected Account() {
	}

	public Account(Customer customer, String accountNumber, String accountName,
			BigDecimal balance, LocalDateTime now) {
		this.customer = customer;
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.balance = balance;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public Long getId() { return id; }
	public Customer getCustomer() { return customer; }
	public String getAccountNumber() { return accountNumber; }
	public String getAccountName() { return accountName; }
	public BigDecimal getBalance() { return balance; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }

	public void deposit(BigDecimal amount, LocalDateTime now) {
		this.balance = this.balance.add(amount);
		this.updatedAt = now;
	}

	public void withdraw(BigDecimal amount, LocalDateTime now) {
		this.balance = this.balance.subtract(amount);
		this.updatedAt = now;
	}
}
