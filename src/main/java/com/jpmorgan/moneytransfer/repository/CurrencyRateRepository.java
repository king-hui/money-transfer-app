package com.jpmorgan.moneytransfer.repository;

import com.jpmorgan.moneytransfer.repository.model.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    @Query("SELECT a FROM CurrencyRate a WHERE a.fromCurrency = :fromCurrency AND a.toCurrency = :toCurrency ")
    Optional<CurrencyRate> findByCurrencyRate(@Param("fromCurrency") String fromCurrency, @Param("toCurrency") String toCurrency);
}