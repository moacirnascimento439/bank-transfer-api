package com.moacir.banktransferapi.service;

import com.moacir.banktransferapi.exception.AccountNotFoundException;
import com.moacir.banktransferapi.exception.DuplicateAccountException;
import com.moacir.banktransferapi.exception.InsufficientBalanceException;
import com.moacir.banktransferapi.model.Account;
import com.moacir.banktransferapi.model.Transaction;
import com.moacir.banktransferapi.model.TransactionType;
import com.moacir.banktransferapi.repository.AccountRepository;
import com.moacir.banktransferapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,
                           TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account createAccount(String accountNumber, String ownerName, BigDecimal initialBalance) {
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new DuplicateAccountException(
                    "Já existe uma conta com o número: " + accountNumber);
        }

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .ownerName(ownerName)
                .balance(initialBalance != null ? initialBalance : BigDecimal.ZERO)
                .build();

        return accountRepository.save(account);
    }

    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Conta não encontrada: " + accountNumber));
    }

    public List<Transaction> getStatement(String accountNumber) {
        Account account = findByAccountNumber(accountNumber);
        return transactionRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        validatePositiveAmount(amount);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Conta não encontrada: " + accountNumber));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        registerTransaction(account, TransactionType.DEPOSIT, amount,
                "Depósito", null);

        return account;
    }

    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        validatePositiveAmount(amount);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Conta não encontrada: " + accountNumber));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Saldo insuficiente na conta: " + accountNumber);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        registerTransaction(account, TransactionType.WITHDRAWAL, amount,
                "Saque", null);

        return account;
    }

    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        validatePositiveAmount(amount);

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException(
                    "Conta de origem e destino não podem ser a mesma");
        }

        // Ordena os números de conta antes de travar, para evitar deadlock
        // quando duas transferências acontecem em sentidos opostos ao mesmo tempo.
        String firstToLock = fromAccountNumber.compareTo(toAccountNumber) < 0
                ? fromAccountNumber : toAccountNumber;
        String secondToLock = fromAccountNumber.compareTo(toAccountNumber) < 0
                ? toAccountNumber : fromAccountNumber;

        accountRepository.findByAccountNumberForUpdate(firstToLock)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Conta não encontrada: " + firstToLock));
        accountRepository.findByAccountNumberForUpdate(secondToLock)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Conta não encontrada: " + secondToLock));

        Account fromAccount = accountRepository.findByAccountNumberForUpdate(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Conta não encontrada: " + fromAccountNumber));
        Account toAccount = accountRepository.findByAccountNumberForUpdate(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Conta não encontrada: " + toAccountNumber));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Saldo insuficiente na conta: " + fromAccountNumber);
        }

        String relatedId = UUID.randomUUID().toString();

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        registerTransaction(fromAccount, TransactionType.TRANSFER_OUT, amount,
                "Transferência para " + toAccountNumber, relatedId);
        registerTransaction(toAccount, TransactionType.TRANSFER_IN, amount,
                "Transferência de " + fromAccountNumber, relatedId);
    }

    private void registerTransaction(Account account, TransactionType type,
                                       BigDecimal amount, String description,
                                       String relatedTransactionId) {
        Transaction transaction = Transaction.builder()
                .account(account)
                .type(type)
                .amount(amount)
                .balanceAfter(account.getBalance())
                .description(description)
                .relatedTransactionId(relatedTransactionId)
                .build();

        transactionRepository.save(transaction);
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero");
        }
    }
}