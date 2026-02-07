package com.mybank.bankingapp.model;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespMessage {
    private String status;
    private String message;
    private Account account;
    private Collection<Account> accounts;
}