package com.moacir.banktransferapi.repository;

import com.moacir.banktransferapi.model.Account;
import com.moacir.banktransferapi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);
}