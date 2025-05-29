package com.jpmorgan.moneytransfer.config;

import com.jpmorgan.moneytransfer.repository.CurrencyRateRepository;
import com.jpmorgan.moneytransfer.repository.model.Account;
import com.jpmorgan.moneytransfer.repository.AccountRepository;
import com.jpmorgan.moneytransfer.repository.model.CurrencyRate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(AccountRepository accountRepository, CurrencyRateRepository currencyRatesRepository) {
        return args -> {
            // Create accounts
            Account account1 = new Account("1", "Alice", new BigDecimal("1000.00"), "USD");
            Account account2 = new Account("2", "Bob", new BigDecimal("500.00"), "JPN");

            accountRepository.save(account1);
            accountRepository.save(account2);

            // Create currencyRates
            CurrencyRate currencyRates = new CurrencyRate("AUD", "USD", new BigDecimal(0.5));
            CurrencyRate currencyRates2 = new CurrencyRate("USD", "JPN", new BigDecimal(144.66));
            CurrencyRate currencyRates3 = new CurrencyRate("AUD", "JPN", new BigDecimal(93.01));
            CurrencyRate currencyRates4 = new CurrencyRate("JPN", "USD", new BigDecimal(0.0069));
            CurrencyRate currencyRates5 = new CurrencyRate("USD", "CNY", new BigDecimal(7.2));
            CurrencyRate currencyRates6 = new CurrencyRate("CNY", "USD", new BigDecimal(0.14));
            CurrencyRate currencyRates7 = new CurrencyRate("CNY", "JPN", new BigDecimal(20.02));

            currencyRatesRepository.save(currencyRates);
            currencyRatesRepository.save(currencyRates2);
            currencyRatesRepository.save(currencyRates3);
            currencyRatesRepository.save(currencyRates4);
            currencyRatesRepository.save(currencyRates5);
            currencyRatesRepository.save(currencyRates6);
            currencyRatesRepository.save(currencyRates7);

        };
    }
}