package com.tesda.banking.customer;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_credentials")
public class CustomerCredential {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "credential_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false, unique = true)
	private Customer customer;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(nullable = false, length = 20)
	private String role;

	@Column(name = "failed_login_attempts", nullable = false)
	private short failedLoginAttempts;

	@Column(name = "locked_until")
	private LocalDateTime lockedUntil;

	@Column(name = "terms_accepted_at", nullable = false)
	private LocalDateTime termsAcceptedAt;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected CustomerCredential() {
	}

	public CustomerCredential(Customer customer, String passwordHash, LocalDateTime now) {
		this.customer = customer;
		this.passwordHash = passwordHash;
		this.role = "CUSTOMER";
		this.failedLoginAttempts = 0;
		this.termsAcceptedAt = now;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public Customer getCustomer() { return customer; }
	public String getPasswordHash() { return passwordHash; }
	public String getRole() { return role; }
	public LocalDateTime getLockedUntil() { return lockedUntil; }

	public void recordSuccessfulLogin(LocalDateTime now) {
		this.failedLoginAttempts = 0;
		this.lockedUntil = null;
		this.lastLoginAt = now;
		this.updatedAt = now;
	}

	public void recordFailedLogin(LocalDateTime now) {
		this.failedLoginAttempts++;
		if (this.failedLoginAttempts >= 5) {
			this.lockedUntil = now.plusMinutes(15);
			this.failedLoginAttempts = 0;
		}
		this.updatedAt = now;
	}
}
