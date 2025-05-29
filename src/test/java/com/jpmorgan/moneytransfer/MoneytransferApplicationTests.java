package com.jpmorgan.moneytransfer;

import com.jpmorgan.moneytransfer.exception.InsufficientFundsException;
import com.jpmorgan.moneytransfer.repository.AccountRepository;
import com.jpmorgan.moneytransfer.repository.model.Account;
import com.jpmorgan.moneytransfer.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class MoneytransferApplicationTests {

	@Autowired
	private AccountService accountService;
	@Autowired
	private AccountRepository accountRepository;

	@BeforeEach
	public void setup() {
		// Find accounts
		Account aliceAccount = accountService.findByAccountNumber("1");
		Account bobAccount = accountService.findByAccountNumber("2");

		aliceAccount.setBalance(new BigDecimal("1000.00"));
		bobAccount.setBalance(new BigDecimal("500.00"));

		accountRepository.save(aliceAccount);
		accountRepository.save(bobAccount);
	}

	@Test
	@DisplayName("Transfer 50 USD from Alice to Bob")
	@Transactional
	public void testTransfer50ToAliceToBob() {

		// Find accounts
		Account aliceAccount = accountService.findByAccountNumber("1");
		Account bobAccount = accountService.findByAccountNumber("2");

		// Transfer amount
		BigDecimal transferAmount = new BigDecimal("50.00");

		// Transfer currency code
		String transferCurrencyCode = "USD";

		// Perform the transfer from Alice to Bob
		accountService.transferMoney(
				aliceAccount.getAccountNumber(),
				bobAccount.getAccountNumber(),
				transferAmount,
				transferCurrencyCode
		);

		// Refresh accounts from database
		Account updatedAliceAccount = accountService.findByAccountNumber(aliceAccount.getAccountNumber());
		Account updatedBobAccount = accountService.findByAccountNumber(bobAccount.getAccountNumber());

		// Verify the new balances
		assertTrue(new BigDecimal("949.5").compareTo(updatedAliceAccount.getBalance()) == 0,
				"Alice's balance should be reduced by 50.5");
		assertTrue(new BigDecimal("7733").compareTo(updatedBobAccount.getBalance()) == 0,
				"Bob's balance should be increased by 7233");
	}

	@Test
	@Transactional
	@DisplayName("Transfer 50 AUD from Bob to Alice recurring for 20 times")
	public void testTransfer50ToBobToAliceFor20Times() {
		// Find accounts
		Account aliceAccount = accountService.findByAccountNumber("1");
		Account bobAccount = accountService.findByAccountNumber("2");

		int transferCount = 0;
		boolean exceptionThrown = false;

		try {
			for (int i = 0; i < 20; i++) {
				// Transfer amount
				BigDecimal transferAmount = new BigDecimal("50.00");

				// Transfer currency code
				String transferCurrencyCode = "AUD";

				transferCount++;

				// Perform the transfer from Bob to Alice
				accountService.transferMoney(
						bobAccount.getAccountNumber(),
						aliceAccount.getAccountNumber(),
						transferAmount,
						transferCurrencyCode
				);
			}
		} catch (InsufficientFundsException e) {
			exceptionThrown = true;
		}

		// Refresh accounts from database
		Account updatedAliceAccount = accountService.findByAccountNumber(aliceAccount.getAccountNumber());
		Account updatedBobAccount = accountService.findByAccountNumber(bobAccount.getAccountNumber());

		// // Assert that Insufficient funds exception was thrown
		assertTrue(exceptionThrown,
				"Insuffient funds exception should not be thrown");
		assertTrue(new BigDecimal("1000").compareTo(updatedAliceAccount.getBalance()) == 0,
				"Alice's balance should be not be increased");
		assertTrue(new BigDecimal("500").compareTo(updatedBobAccount.getBalance()) == 0,
				"Bob's balance should be not be decreased");
	}

	@Test
	@DisplayName("Concurrently Transfer money")
	public void testConcurrentTransferMoney() throws InterruptedException {
		String aliceAccountNumber = "1";
		String bobAccountNumber = "2";
		List<Runnable> transfers = Arrays.asList(
				// Transfer 1: 20 AUD from Bob to Alice
				() -> accountService.transferMoney(bobAccountNumber, aliceAccountNumber, new BigDecimal("20.00"), "AUD"),

				// Transfer 2: 40 USD from Alice to Bob
				() -> accountService.transferMoney(aliceAccountNumber, bobAccountNumber, new BigDecimal("40.00"), "USD"),

				// Transfer 3: 40 CNY from Alice to Bob
				() -> accountService.transferMoney(aliceAccountNumber, bobAccountNumber, new BigDecimal("40.00"), "CNY")
		);

		// 3. Set up concurrent execution
		CountDownLatch startLatch = new CountDownLatch(1); // To ensure all threads start at the same time
		CountDownLatch finishLatch = new CountDownLatch(transfers.size());
		List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

		// 4. Start each transfer in its own thread
		for (Runnable transfer : transfers) {
			new Thread(() -> {
				try {
					startLatch.await(); // Wait for all threads to be ready
					transfer.run();
				} catch (Exception e) {
					exceptions.add(e);
                } finally {
					finishLatch.countDown();
				}
			}).start();
		}

		// 5. Start all transfers simultaneously
		startLatch.countDown();

		// 6. Wait for all transfers to complete
		finishLatch.await(10, TimeUnit.SECONDS);

		// 7. Refresh account data from the database
		Account updatedAliceAccount = accountService.findByAccountNumber("1");
		Account updatedBobAccount = accountService.findByAccountNumber("2");

		if (!exceptions.isEmpty()) {
			// If insufficient fund on Bob's account, so Bob not able to transfer 20 AUD to Alice
			assertEquals(new BigDecimal("953.9440"), updatedAliceAccount.getBalance(),
					"Alice's balance should have changed");
			assertEquals(new BigDecimal("7087.2000"), updatedBobAccount.getBalance(),
					"Bob's balance should have changed");
		} else {
			// If sufficient fund on Bob's account, so Bob able to transfer 20 AUD to Alice
			assertEquals(new BigDecimal("963.9440"), updatedAliceAccount.getBalance(),
					"Alice's balance should have changed");
			assertEquals(new BigDecimal("5208.3980"), updatedBobAccount.getBalance(),
					"Bob's balance should have changed");
		}

	}
}
