package com.bank.dto;

import java.time.LocalDateTime;
import java.util.List;

//handles all requests/response of data
public class DTORequest {

    private DTORequest () {

    }

    //for authentication, register, and login
    public static class LoginRequest {
        private String username;
        private String password;

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
        private String username;
        private String password;
        private String email;
        private String firstName;
        private String lastName;
        private String accountType;
        private Double initialDebitBalance;
        private Double initialSavingsBalance;


        public RegisterRequest() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
        //getter and setter
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
        public String getEmail() {
            return email;
        }
        public void setEmail(String e) {
            this.email = e;
        }
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String f) {
            this.firstName = f;
        }
        public String getLastName() { return lastName; }
        public void setLastName(String l) { this.lastName = l; }
        public String getFullName() { return firstName + " " + lastName; }
        public String getAccountType() { return accountType; }
        public void setAccountType(String t) { this.accountType =  t; }
        public Double getInitialDebitBalance() { return initialDebitBalance; }
        public void setInitialDebitBalance(Double b) { this.initialDebitBalance = b; }
        public Double getInitialSavingsBalance() { return initialSavingsBalance; }
        public void setInitialSavingsBalance(Double b) { this.initialSavingsBalance = b; }
    }

    public static class AuthResponse {
        private String token;
        private String message;
        private Long userId;
        private List<AccountResponse> accounts;

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

    //for account response
    public static class AccountResponse {
        private Long id;
        private Long userId;
        private String accountNumber;
        private Double balance;
        private String accountType;
        private LocalDateTime creationAt;
        //1.1.0
        private Double creditLimit;
        static Double interestRate;

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
        //1.2.0
        public void setCreditLimit(Double limit) {
            this.creditLimit = limit;
        }
        public Double getInterestRate(Double rate) { return rate; }
        public void setInterestRate(Double rate) {
            this.interestRate = rate;
        }
    }

    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String role;
        private LocalDateTime createdAt;
        private List<AccountResponse> accounts;

        public UserResponse() {}

        public UserResponse(Long id, String username, String email, String role, LocalDateTime createdAt, List<AccountResponse> accounts) {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String u) { this.username = u; }
        public String getEmail() { return email; }
        public void setEmail(String e) { this.email = e; }
        public String getRole() { return role; }
        public void setRole(String r) { this.role = r; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
        public List<AccountResponse> getAccounts() { return accounts; }
        public void setAccounts(List<AccountResponse> a) { this.accounts = a; }
    }


    //for transaction requests
    public static class TransferRequest {
        private Long fromAccountId;
        private Long toAccountId;
        private Double amount;
        private String description;

        public TransferRequest() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
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
        private Long accountId;
        private Double amount;

        public DepositRequest() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
        //getters and setters
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long id) { this.accountId = id; }
        public Double getAmount() { return amount; }
        public void setAmount(Double a) { this.amount = a; }
    }

    public static class WithdrawRequest {
        private Long accountId;
        private Double amount;

        public WithdrawRequest() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
        //getters and setters
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long id) { this.accountId = id; }
        public Double getAmount() { return amount; }
        public void setAmount(Double a) { this.amount = a; }
    }

    public static class CreateSavingsRequest {
        public Double initialBalance;

        public CreateSavingsRequest() {}
        public CreateSavingsRequest(Double initialBalance) {
            this.initialBalance = initialBalance;
        }
    }

    public static class UpdateInterestRateRequest {
        public Double interestRate;

        public UpdateInterestRateRequest() {}
        public UpdateInterestRateRequest(Double interestRate) {
            this.interestRate = interestRate;
        }
        public Double getInterestRate() { return interestRate; }
        public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
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

        private TransactionResponse(Builder builder) {
            this.id = builder.id;
            this.fromAccountId = builder.fromAccountId;
            this.toAccountId = builder.toAccountId;
            this.userId = builder.userId;
            this.amount = builder.amount;
            this.type = builder.type;
            this.status = builder.status;
            this.description = builder.description;
            this.dateTime = builder.dateTime;
            this.availableBalance = builder.availableBalance;
        }

        // Default constructor
        public TransactionResponse() {}


        // Builder class
        public static class Builder {
            private Long id;
            private Long fromAccountId;
            private Long toAccountId;
            private Long userId;
            private Double amount;
            private String type;
            private String status;
            private String description;
            private LocalDateTime dateTime;
            private Double availableBalance;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder fromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; return this; }
            public Builder toAccountId(Long toAccountId) { this.toAccountId = toAccountId; return this; }
            public Builder userId(Long userId) { this.userId = userId; return this; }
            public Builder amount(Double amount) { this.amount = amount; return this; }
            public Builder type(String type) { this.type = type; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder dateTime(LocalDateTime dateTime) { this.dateTime = dateTime; return this; }
            public Builder availableBalance(Double availableBalance) { this.availableBalance = availableBalance; return this; }

            public TransactionResponse build() {
                return new TransactionResponse(this);
            }
        }

        //getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(Long id) { this.fromAccountId = id; }
        public Long getToAccountId() { return toAccountId; }
        public void setToAccountId(Long id) { this.toAccountId = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long id) { this.userId = id; }
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

    //Admin access - update credit
    public static class UpdateCreditBalanceRequest {
        private String type; // balance or limit
        private Double amount;
        private Double balance;
        private Double creditLimit;

        public UpdateCreditBalanceRequest() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
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

    //General responses
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

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
        private int status;
        private String error;
        private String message;
        private long timestamp;

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

