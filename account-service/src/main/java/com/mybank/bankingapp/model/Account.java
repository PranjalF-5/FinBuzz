package com.mybank.bankingapp.model;


import com.mybank.bankingapp.exception.InvalidAccountException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name="accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private double balance;
    private double roi;
    @Enumerated(EnumType.STRING)
    private AccountType type;
    private boolean active = true;

    public Account(AccountType type, double balance, String name) throws InvalidAccountException {
        validateInput(type, balance, name);
        this.type = type;
        this.balance = balance;
        this.name = name;
    }
    public Account(String name, double balance) throws InvalidAccountException {
        this.balance = balance;
        this.name = name;
    }

    public Account(int id, String name, double balance) {
        this.id = id;
        this.balance = balance;
        this.name = name;
    }

    public Account(int id, String name, double balance, AccountType accountType) {
        this.id = id;
        this.type = accountType;
        this.balance = balance;
        this.name = name;
    }

    public Account(AccountType accountType, String s, int i) {

        this.type = accountType;
        this.balance = i;
        this.name = s;
        this.active = true;
    }

    private void validateInput(AccountType type, double balance, String name) throws InvalidAccountException {
        if (type == null) {
            throw new InvalidAccountException("Account type cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidAccountException("Account name cannot be empty");
        }
        if (balance < 0) {
            throw new InvalidAccountException("Initial balance cannot be negative");
        }
    }

    // Existing getters and setters with added validations
    public void setBalance(double balance) throws InvalidAccountException {
        if (balance < 0) {
            throw new InvalidAccountException("Balance cannot be negative");
        }
        this.balance = balance;
    }
    public String toString()  {
        return ("Account ID: " + id + ", Name: " + name + ", Balance: " + balance + ", Account Type: " + type);

    }

    public void setRoi(double roi) throws InvalidAccountException {
        if (roi < 0) {
            throw new InvalidAccountException("Rate of interest cannot be negative");
        }
        this.roi = roi;
    }

    // Other getters and setters remain the same
    public int getId() { return id; }
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public double getRoi() { return roi; }
    public AccountType getType() { return type; }
    public boolean isActive() { return active; }
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(AccountType type) { this.type = type; }
    public void setActive(boolean active) { this.active = active; }
}