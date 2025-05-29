package com.jpmorgan.moneytransfer.service;

import com.jpmorgan.moneytransfer.repository.CurrencyRateRepository;
import com.jpmorgan.moneytransfer.repository.model.CurrencyRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CurrencyRateService {

    private final CurrencyRateRepository currencyRateRepository;

    @Autowired
    public CurrencyRateService(CurrencyRateRepository currencyRateRepository) {
        this.currencyRateRepository = currencyRateRepository;
    }

    @Transactional(readOnly = true)
    public CurrencyRate findCurrencyRate(String fromCurrency, String toCurrency) {
        return currencyRateRepository.findByCurrencyRate(fromCurrency, toCurrency)
                .orElseThrow(() -> new RuntimeException("Currency Rate from "+ fromCurrency + " to " + toCurrency + " not found"));
    }

    public BigDecimal computeExchangeRate(String fromCurrency, String toCurrency, BigDecimal amount) {
        CurrencyRate currencyRate = findCurrencyRate(fromCurrency, toCurrency);
        return amount.multiply(currencyRate.getRate());
    }
}