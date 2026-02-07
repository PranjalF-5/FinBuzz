package com.mybank.bankingapp.controller;

import com.mybank.bankingapp.exception.InvalidAccountException;
import com.mybank.bankingapp.model.*;
import com.mybank.bankingapp.service.AccountService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private AccountService accServices;
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RespMessage> createOrder(@RequestBody Account account){
        int id;
        try {
            id = accServices.createAccount(account);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(RespMessage
                    .builder()
                    .status("False")
                    .message("Couldn't Create Account")
                    .build());
        }
        log.info("Account created successfully");
        return ResponseEntity.
                ok().
                body(RespMessage.builder().
                        status("true").
                        message("Account created successfully with id - " + id).build());
    }
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RespMessage> getAccounts() {
        Collection<Account> allAccounts = null;
        try {
            allAccounts = accServices.getAllAccounts();
//            System.out.println("hi");
        }catch (Exception e) {
            log.info("Error getting accounts");
            return ResponseEntity.badRequest().body(RespMessage
                    .builder()
                    .status("False")
                    .message("Couldn't Retrieve Accounts")
                    .build());
        }
        log.info("Successfully retrieved all accounts");
        return ResponseEntity
                .ok()
                .body(RespMessage
                        .builder()
                        .status("true")
                        .message("All accounts retrieved successfully")
                        .accounts(allAccounts)
                        .build());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable int id) {
        try {
            Account account = accServices.getAccount(id);
            if (account != null) {
                return ResponseEntity.ok(account);
            }
            return ResponseEntity.notFound().build();
        } catch (InvalidAccountException e) {
            log.error("Invalid account ID: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching account with ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<RespMessage> updateAccount(@PathVariable int id, @RequestBody Account account) {
        try {
            boolean updated = accServices.updateAccount(id, account);
            if (updated) {
                return ResponseEntity.ok(RespMessage.builder()
                        .status("true")
                        .message("Account updated successfully")
                        .build());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RespMessage.builder()
                    .status("false")
                    .message("Account not found")
                    .build());
        } catch (Exception e) {
            log.error("Error updating account with ID: {}", id, e);
            return ResponseEntity.internalServerError().body(RespMessage.builder()
                    .status("false")
                    .message("Error updating account")
                    .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespMessage> deleteAccount(@PathVariable int id) {
        try {
            boolean deleted = accServices.deleteAccount(id);
            if (deleted) {
                return ResponseEntity.ok(RespMessage.builder()
                        .status("true")
                        .message("Account deleted successfully")
                        .build());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RespMessage.builder()
                    .status("false")
                    .message("Account not found")
                    .build());
        } catch (Exception e) {
            log.error("Error deleting account with ID: {}", id, e);
            return ResponseEntity.internalServerError().body(RespMessage.builder()
                    .status("false")
                    .message("Error deleting account")
                    .build());
        }
    }

    @GetMapping("/high-balance")
    public ResponseEntity<Map<String, Long>> getAccountsWithHighBalance() {
        try {
            long count = accServices.getAccountsWithBalanceGreaterThan1Lac();
            Map<String, Long> response = Map.of("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching high balance accounts count", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-type")
    public ResponseEntity<Map<String, Long>> getAccountsByType() {
        try {
            Map<String, Long> accountsByType = accServices.getAccountsByType();
            return ResponseEntity.ok(accountsByType);
        } catch (Exception e) {
            log.error("Error fetching accounts by type", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-type/sorted")
    public ResponseEntity<Map<String, Long>> getAccountsByTypeSorted() {
        try {
            Map<String, Long> accountsByTypeSorted = accServices.getAccountsByTypeSorted();
            return ResponseEntity.ok(accountsByTypeSorted);
        } catch (Exception e) {
            log.error("Error fetching sorted accounts by type", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/average-balance")
    public ResponseEntity<Map<String, Double>> getAverageBalanceByType() {
        try {
            Map<String, Double> avgBalanceByType = accServices.getAvgBalanceByType();
            return ResponseEntity.ok(avgBalanceByType);
        } catch (Exception e) {
            log.error("Error fetching average balance by type", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<List<Integer>> getAccountIdsByName(@PathVariable String name) {
        try {
            List<Integer> accountIds = accServices.getAccountIdsByExactName(name);
            if (accountIds.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(accountIds);
        } catch (Exception e) {
            log.error("Error searching accounts by name: {}", name, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    @PostMapping("/{id}/deposits")
    public ResponseEntity<ResponseMessage<Account>> deposit(@PathVariable int id,
                                                            @Valid @RequestBody DepositRequest request) throws InvalidAccountException {
        ResponseMessage<Account> response = accServices.deposit(id, request.getAmount());
        return response.getStatus().equals("success")
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }



    //    POST /accounts/{id}/withdrawal [SYNC - REST API]
    @PostMapping("/{id}/withdrawal")
    public ResponseEntity<ResponseMessage<Account>> withdraw(@PathVariable int id,
                                                             @Valid @RequestBody WithdrawalRequest request) throws InvalidAccountException {
        ResponseMessage<Account> response = accServices.withdraw(id, request.getAmount());
        return response.getStatus().equals("success")
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
