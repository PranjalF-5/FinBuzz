package com.mybank.bankingapp.model;

public enum AccountType {
    SAVINGS, CURRENT, DEPOSIT, LOAN;
    private double roi;
    AccountType(double roi){
        this.roi=roi;
    }

    AccountType() {

    }

    public double getRoi() {
        return roi;
    }
}
