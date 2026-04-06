package com.bank.dto;

import java.time.LocalDateTime;
import java.util.List;

//handles all requests/response of data
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
        public void setUsername(String u) {
            this.username = u;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String ps) {
            this.password = ps;
        }
    }

    public static class RegisterRequest {
        public String username;
        public String password;
        public String email;
        public String firstName;
        public String lastName;
        public String accountType;
        public Double initialDebitBalance;


        public RegisterRequest() {}
        //getter and setter
        public String getUsername() {
            return username;
        }
        public void setUsername(String u) {
            this.username = username = u;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String ps) {
            this.password = password = ps;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String e) {
            this.email = email = e;
        }
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String f) {
            this.firstName = firstName = f;
        }
        public String getLastName() { return lastName; }
        public void setLastName(String l) { this.lastName = l; }
        public String getFullName() { return firstName + " " + lastName; }
        public String getAccountType() { return accountType; }
        public void setAccountType(String t) { this.accountType = accountType = t; }
        public Double getInitialDebitBalance() { return initialDebitBalance; }
        public void setInitialDebitBalance(Double b) { this.initialDebitBalance = b; }
    }

    public static class AuthResponse {
        public String token;
        public String message;
        public Long userId;
        public List<AccountResponse> accounts;

        public AuthResponse() {}
        public AuthResponse(String token, String message, Long id, List<AccountResponse> accounts) {
            this.token = token;
            this.message = message;
            this.userId = id;
            this.accounts = accounts;
        }
        //getter and setter
        public String getToken() {return token;}
        public void setToken(String t) {
            this.token = t;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String m) {
            this.message = m;
        }
        public Long getUserId() {
            return userId;
        }
        public void setUserId(Long id) {
            this.userId = id;
        }
        public List<AccountResponse> getAccounts() { return accounts; }
        public void setAccounts(List<AccountResponse> a) { this.accounts = a; }
    }

    //Account
    public static class AccountResponse {
        public Long id;
        public Long userId;
        public String accountNumber;
        public Double balance;
        public String accountType;
        public LocalDateTime creationAt;
        //1.1.0
        public Double creditLimit;

        public AccountResponse() {}
        public AccountResponse(Long id, Long userId, String accountNumber, Double balance, String accountType, LocalDateTime creationAt) {
            this.id = id;
            this.userId = userId;
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.accountType = accountType;
            this.creationAt = creationAt;
        }
        //getter and setter
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public Long getUserId() {return userId;}
        public void setUserId(Long userId) {this.userId = userId;}
        public String getAccountNumber() {
            return accountNumber;
        }
        public void setAccountNumber(String n) {
            this.accountNumber = n;
        }
        public Double getBalance() {
            return balance;
        }
        public void setBalance(Double b) {
            this.balance = b;
        }
        public String getAccountType() {
            return accountType;
        }
        public void setAccountType(String t) {
            this.accountType = t;
        }
        public LocalDateTime getCreationAt() {return creationAt;}
        public void setCreationAt(LocalDateTime c) {this.creationAt = c;}
        //1.1.0
        public Double getCreditLimit() {return creditLimit;}
        public void setCreditLimit(Double l) {this.creditLimit = l;}
    }

    public static class UserResponse {
        public Long id;
        public String username;
        public String email;
        public String firstName;
        public String lastName;
        public String role;
        public LocalDateTime createdAt;
        public List<AccountResponse> accounts;

        public UserResponse() {}

        public UserResponse(Long id, String username, String email, String firstName, String lastName,
                            String role, LocalDateTime createdAt, List<AccountResponse> accounts) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.createdAt = createdAt;
            this.accounts = accounts;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String u) { this.username = u; }
        public String getEmail() { return email; }
        public void setEmail(String e) { this.email = e; }
        public String getFirstName() { return firstName; }
        public void getFirstName(String f) { this.firstName = f; }
        public String getLastName() { return lastName; }
        public void setLastName(String l) { this.lastName = l; }
        public String getFullName() { return firstName + " " + lastName; }
        public String getRole() { return role; }
        public void setRole(String r) { this.role = r; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
        public List<AccountResponse> getAccounts() { return accounts; }
        public void setAccounts(List<AccountResponse> a) { this.accounts = a; }
    }


    //Transaction
    public static class TransferRequest {
        public Long fromAccountId;
        public Long toAccountId;
        public Double amount;
        public String description;

        public TransferRequest() {}
        //getters and setters
        public Long getFromAccountId() {
            return fromAccountId;
        }
        public void setFromAccountId(Long id) {
            this.fromAccountId = id;
        }
        public Long getToAccountId() {
            return toAccountId;
        }
        public void setToAccountId(Long id) {
            this.toAccountId = id;
        }
        public Double getAmount() {
            return amount;
        }
        public void setAmount(Double a) {
            this.amount = a;
        }
        public String getDescription() {return description;}
        public void setDescription(String d) {this.description = d;}

    }

    public static class DepositRequest {
        public Long accountId;
        public Double amount;

        public DepositRequest() {}
        //getters and setters
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long id) { this.accountId = id; }
        public Double getAmount() { return amount; }
        public void setAmount(Double a) { this.amount = a; }
    }

    public static class WithdrawRequest {
        public Long accountId;
        public Double amount;

        public WithdrawRequest() {}
        //getters and setters
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long id) { this.accountId = id; }
        public Double getAmount() { return amount; }
        public void setAmount(Double a) { this.amount = a; }
    }

    public static class TransactionResponse {
        public Long id;
        public Long fromAccountId;
        public Long toAccountId;
        //1.1.0
        public Long userId;
        public Double amount;
        public String type; // "TRANSFER", "DEPOSIT", "WITHDRAWAL"
        public String status; // "COMPLETED", "PENDING", "FAILED"
        public String description;
        public LocalDateTime dateTime;
        public Double availableBalance;

        public TransactionResponse() {}
        public TransactionResponse(Long id, Long fromAccountId, Long toAccountId, Long userId, Double amount,
                              String type, String Status, String description, LocalDateTime dateTime) {
            this.id = id;
            this.fromAccountId = fromAccountId;
            this.toAccountId = toAccountId;
            this.userId = userId;
            this.amount = amount;
            this.type = type;
            this.status = Status;
            this.description = description;
            this.dateTime = dateTime;
            this.availableBalance = null;
        }

        public TransactionResponse(Long id, Long fromAccountId, Long toAccountId, Long userId, Double amount,
                                   String type, String Status, String description, LocalDateTime dateTime, Double availableBalance) {
            this.id = id;
            this.fromAccountId = fromAccountId;
            this.toAccountId = toAccountId;
            this.userId = userId;
            this.amount = amount;
            this.type = type;
            this.status = Status;
            this.description = description;
            this.dateTime = dateTime;
            this.availableBalance = availableBalance;
        }

        //getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(Long id) { this.fromAccountId = id; }
        public Long getToAccountId() { return toAccountId; }
        public void setToAccountId(Long id) { this.toAccountId = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = id; }
        public Double getAmount() { return amount; }
        public void setAmount(Double a) { this.amount = a; }
        public String getType() { return type; }
        public void setType(String t) { this.type = t; }
        public String getStatus() { return status; }
        public void setStatus(String s) { this.status = s; }
        public String getDescription() { return description; }
        public void setDescription(String d) { this.description = d; }
        public LocalDateTime getDateTime() { return dateTime; }
        public void setDateTime(LocalDateTime dt) { this.dateTime = dt; }
        public Double getAvailableBalance() { return availableBalance; }
        public void setAvailableBalance(Double a) { this.availableBalance = a; }
    }

    //Admin - update credit
    public static class UpdateCreditBalanceRequest {
        public String type; // balance or limit
        public Double amount;
        public Double balance;
        public Double creditLimit;

        public UpdateCreditBalanceRequest() {}
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Double getAmount() { return amount; }
        public void setAmount(Double a) { this.amount = a; }

        public Double getBalance() {
            return balance;
        }

        public void setBalance(Double balance) {
            this.balance = balance;
        }

        public Double getCreditLimit() {
            return creditLimit;
        }

        public void setCreditLimit(Double creditLimit) {
            this.creditLimit = creditLimit;
        }
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
        public void setSuccess(boolean s) { this.success = s; }
        public String getMessage() { return message; }
        public void setMessage(String m) { this.message = m; }
        public T getData() { return data; }
        public void setData(T d) { this.data = d; }
    }

    public static class ErrorResponse {
        public int status;
        public String error;
        public String message;
        public long timestamp;

//        public ErrorResponse() {}
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
        public void setTimestamp(long t) { this.timestamp = t;}
    }
}

