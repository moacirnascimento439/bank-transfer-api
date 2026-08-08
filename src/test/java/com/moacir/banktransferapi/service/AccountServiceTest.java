package com.moacir.banktransferapi.service;

import com.moacir.banktransferapi.exception.AccountNotFoundException;
import com.moacir.banktransferapi.exception.DuplicateAccountException;
import com.moacir.banktransferapi.exception.InsufficientBalanceException;
import com.moacir.banktransferapi.model.Account;
import com.moacir.banktransferapi.repository.AccountRepository;
import com.moacir.banktransferapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .accountNumber("12345-6")
                .ownerName("Moacir Nascimento")
                .balance(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    void deveCriarContaComSucesso() {
        when(accountRepository.existsByAccountNumber("12345-6")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(
                "12345-6", "Moacir Nascimento", new BigDecimal("1000.00"));

        assertThat(result.getAccountNumber()).isEqualTo("12345-6");
        assertThat(result.getBalance()).isEqualByComparingTo("1000.00");
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void deveLancarExcecaoAoCriarContaDuplicada() {
        when(accountRepository.existsByAccountNumber("12345-6")).thenReturn(true);

        assertThatThrownBy(() ->
                accountService.createAccount("12345-6", "Outro Nome", BigDecimal.ZERO))
                .isInstanceOf(DuplicateAccountException.class)
                .hasMessageContaining("12345-6");

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deveDepositarComSucesso() {
        when(accountRepository.findByAccountNumberForUpdate("12345-6"))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.deposit("12345-6", new BigDecimal("500.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("1500.00");
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void deveLancarExcecaoAoDepositarValorNegativo() {
        assertThatThrownBy(() ->
                accountService.deposit("12345-6", new BigDecimal("-100.00")))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository);
    }

    @Test
    void deveSacarComSucessoQuandoSaldoSuficiente() {
        when(accountRepository.findByAccountNumberForUpdate("12345-6"))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.withdraw("12345-6", new BigDecimal("300.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("700.00");
    }

    @Test
    void deveLancarExcecaoAoSacarComSaldoInsuficiente() {
        when(accountRepository.findByAccountNumberForUpdate("12345-6"))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() ->
                accountService.withdraw("12345-6", new BigDecimal("5000.00")))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deveLancarExcecaoAoBuscarContaInexistente() {
        when(accountRepository.findByAccountNumber("00000-0"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findByAccountNumber("00000-0"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void deveTransferirComSucessoEntreContas() {
        Account contaDestino = Account.builder()
                .id(2L)
                .accountNumber("98765-4")
                .ownerName("Ana Beatriz")
                .balance(new BigDecimal("200.00"))
                .build();

        when(accountRepository.findByAccountNumberForUpdate(anyString()))
                .thenReturn(Optional.of(account))
                .thenReturn(Optional.of(contaDestino))
                .thenReturn(Optional.of(account))
                .thenReturn(Optional.of(contaDestino));

        accountService.transfer("12345-6", "98765-4", new BigDecimal("300.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("700.00");
        assertThat(contaDestino.getBalance()).isEqualByComparingTo("500.00");
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    void deveLancarExcecaoAoTransferirParaMesmaConta() {
        assertThatThrownBy(() ->
                accountService.transfer("12345-6", "12345-6", new BigDecimal("100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mesma");
    }
}