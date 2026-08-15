package com.tesda.banking.account;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Pattern(regexp = "^[0-9+() -]{7,30}$", message = "must be a valid phone number") String phoneNumber,
		@NotNull @Past LocalDate dateOfBirth,
		@NotBlank @Size(min = 8, max = 72) String password,
		@AssertTrue(message = "must be accepted") boolean acceptedTerms,
		@DecimalMin(value = "0.00") @Digits(integer = 13, fraction = 2) BigDecimal openingDeposit) {
}
