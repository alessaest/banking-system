package com.bank.dto;

import java.time.LocalDateTime;

public class DTORequest {

    //Authentication
    public static class LoginRequest {
        public String username;
        public String password;
        public LoginRequest() {}
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        //getter and setter for login
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        public String username;
        public String password;
        public String email;
        public String fullName;
        public RegisterRequest() {}
        public RegisterRequest(String username, String password, String email, String fullName) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.fullName = fullName;
        }
        //getter and setter
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getFullName() {
            return fullName;
        }
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    public static class AuthResponse {
        public String token;
        public String message;
        public Long userId;
        public AuthResponse() {}
        public AuthResponse(String token, String message, Long userId) {
            this.token = token;
            this.message = message;
            this.userId = userId;
        }
        //getter and setter
        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public Long getUserId() {
            return userId;
        }
        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }

    //Account
    public static class AccountRequest {
        public Long id;
        public String accountNumber;
        public Double balance;
        public String accountType;
        public Long userId;
        public AccountRequest() {}
        public AccountRequest(Long id, String accountNumber, Double balance, String accountType) {
            this.id = id;
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.accountType = accountType;
            this.userId = id;
        }
        //getter and setter
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getAccountNumber() {
            return accountNumber;
        }
        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }
        public Double getBalance() {
            return balance;
        }
        public void setBalance(Double balance) {
            this.balance = balance;
        }
        public String getAccountType() {
            return accountType;
        }
        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }
        public Long getUserId() {
            return userId;
        }
        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }

    public static class createAccountRequest {
        public String accountNumber;
        public String accountType;
        public createAccountRequest() {}
        public createAccountRequest(String accountNumber, String accountType) {
            this.accountNumber = accountNumber;
            this.accountType = accountType;
        }
        //getters and setters
        public String getAccountNumber() {
            return accountNumber;
        }
        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }
        public String getAccountType() {
            return accountType;
        }
        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }
    }

    //Transaction
    public static class TransferRequest {
        public Long fromAccountId;
        public Long toAccountId;
        public Double amount;
        public TransferRequest() {}
        public TransferRequest(Long fromAccountId, Long toAccountId, Double amount) {
            this.fromAccountId = fromAccountId;
            this.toAccountId = toAccountId;
            this.amount = amount;
        }
        //getters and setters
        public Long getFromAccountId() {
            return fromAccountId;
        }
        public void setFromAccountId(Long fromAccountId) {
            this.fromAccountId = fromAccountId;
        }
        public Long getToAccountId() {
            return toAccountId;
        }
        public void setToAccountId(Long toAccountId) {
            this.toAccountId = toAccountId;
        }
        public Double getAmount() {
            return amount;
        }
        public void setAmount(Double amount) {
            this.amount = amount;
        }
    }

    public static class DepositRequest {
        public Long accountId;
        public Double amount;
        public DepositRequest() {}
        public DepositRequest(Long accountId, Double amount) {
            this.accountId = accountId;
            this.amount = amount;
        }
        //getters and setters
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    public static class WithdrawRequest {
        public Long accountId;
        public Double amount;
        public WithdrawRequest() {}
        public WithdrawRequest(Long accountId, Double amount) {
            this.accountId = accountId;
            this.amount = amount;
        }
        //getters and setters
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    public static class TransactionDTO {
        public Long id;
        public Long fromAccountId;
        public Long toAccountId;
        public Double amount;
        public String type; // "TRANSFER", "DEPOSIT", "WITHDRAWAL"
        public LocalDateTime dateTime;
        public String status; // "COMPLETED", "PENDING", "FAILED"
        public TransactionDTO() {}
        public TransactionDTO(Long id, Long fromAccountId, Long toAccountId, Double amount,
                              String type, LocalDateTime dateTime, String status) {
            this.id = id;
            this.fromAccountId = fromAccountId;
            this.toAccountId = toAccountId;
            this.amount = amount;
            this.type = type;
            this.dateTime = dateTime;
            this.status = status;
        }
        //getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }
        public Long getToAccountId() { return toAccountId; }
        public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public LocalDateTime getDateTime() { return dateTime; }
        public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    //Generic
    public static class ApiResponse<T> {
        public boolean success;
        public String message;
        public T data;
        public ApiResponse() {}
        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        //getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }

    public static class ErrorResponse {
        public int status;
        public String error;
        public String message;
        public long timestamp;
        public ErrorResponse() {}
        public ErrorResponse(int status, String error, String message) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        //getters and setters
        public int getStatus() { return status; }
        public void setStatus(int s) { this.status = s; }
        public String getError() { return error; }
        public void setError(String e) { this.error = e; }
        public String getMessage() { return message; }
        public void setMessage(String m) { this.message = m; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp;}
    }
}

