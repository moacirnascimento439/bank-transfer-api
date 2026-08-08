package com.moacir.banktransferapi.controller;

import com.moacir.banktransferapi.dto.*;
import com.moacir.banktransferapi.model.Account;
import com.moacir.banktransferapi.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        Account account = accountService.createAccount(
                request.accountNumber(),
                request.ownerName(),
                request.initialBalance()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AccountResponse.fromEntity(account));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber) {

        Account account = accountService.findByAccountNumber(accountNumber);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @GetMapping("/{accountNumber}/statement")
    public ResponseEntity<List<TransactionResponse>> getStatement(
            @PathVariable String accountNumber) {

        List<TransactionResponse> statement = accountService.getStatement(accountNumber)
                .stream()
                .map(TransactionResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(statement);
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {

        Account account = accountService.deposit(accountNumber, request.amount());
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {

        Account account = accountService.withdraw(accountNumber, request.amount());
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @Valid @RequestBody TransferRequest request) {

        accountService.transfer(
                request.fromAccountNumber(),
                request.toAccountNumber(),
                request.amount()
        );

        return ResponseEntity.ok().build();
    }
}