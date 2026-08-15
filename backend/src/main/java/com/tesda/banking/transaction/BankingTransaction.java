package com.tesda.banking.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tesda.banking.account.Account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class BankingTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_number", referencedColumnName = "account_number", nullable = false)
	private Account account;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", nullable = false)
	private TransactionType transactionType;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	@Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
	private BigDecimal balanceAfter;

	@Column(name = "reference_number", nullable = false, unique = true, length = 30)
	private String referenceNumber;

	@Column(length = 255)
	private String remarks;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected BankingTransaction() {
	}

	public BankingTransaction(Account account, TransactionType transactionType, BigDecimal amount,
			BigDecimal balanceAfter, String referenceNumber, String remarks, LocalDateTime now) {
		this.account = account;
		this.transactionType = transactionType;
		this.amount = amount;
		this.balanceAfter = balanceAfter;
		this.referenceNumber = referenceNumber;
		this.remarks = remarks;
		this.createdAt = now;
	}

	public String getReferenceNumber() { return referenceNumber; }
	public Long getId() { return id; }
	public Account getAccount() { return account; }
	public TransactionType getTransactionType() { return transactionType; }
	public BigDecimal getAmount() { return amount; }
	public BigDecimal getBalanceAfter() { return balanceAfter; }
	public String getRemarks() { return remarks; }
	public LocalDateTime getCreatedAt() { return createdAt; }
}
