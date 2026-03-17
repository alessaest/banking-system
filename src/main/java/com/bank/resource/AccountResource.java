package com.bank.resource;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.service.AccountService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Accounts", description = "Account Management endpoints")
public class AccountResource {

    @Inject
    AccountService accountService;
    @POST
    @Operation(summary = "Create new account", description = "Create a new debit or credit account for a user")
    @APIResponse(responseCode = "201", description = "Account created successfully", content = @Content(schema = @Schema(implementation = Account.class)))
    @APIResponse(responseCode = "400", description = "Invalid input data")
    public Response createAccount(@Parameter(description = "User ID", required = true) @QueryParam("userId") Long userId,
                                  @Parameter(description = "Account type (DEBIT/CREDIT)", required = true) @QueryParam("accountType") String accountType,
                                  @Parameter(description = "Initial balance (optional)") @QueryParam("initialBalance") Double initialBalance) {
        try {
            Account account = accountService.createAccount(userId, accountType, initialBalance);
            return Response.status(Response.Status.CREATED).entity(account).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(400, "Bad Request", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/account/{id}")
    @Operation(summary = "Get account details", description = "Retrieve account information by specific ID")
    @APIResponse(responseCode = "200", description = "Account found", content = @Content(schema = @Schema(implementation = Account.class)))
    @APIResponse(responseCode = "404", description = "Account not found")

    public Response getAccount( @Parameter(description = "Account ID", required = true) @PathParam("id") Long accountId) {
        try {
            Optional<Account> account = accountService.getAccountById(accountId);
            if (account.isPresent()) {
                return Response.ok(account.get()).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity(new DTORequest.ErrorResponse(404, "Not Found", "Account not found")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/user/{userId}")
    @Operation(summary = "Get user accounts", description = "Retrieve all accounts for a specific user")
    @APIResponse(responseCode = "200", description = "Accounts retrieved successfully", content = @Content(schema = @Schema(implementation = Account.class)))

    public Response getUserAccounts( @Parameter(description = "User ID", required = true) @PathParam("userId") Long userId) {
        try {
            List<Account> accounts = accountService.getUserAccounts(userId);
            return Response.ok(accounts).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Check the current balance of an account")
    @APIResponse(responseCode = "404", description = "Account not found")

    public Response getBalance(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId) {
        try {
            Double balance = accountService.getAccountBalance(accountId);
            return Response.ok(new DTORequest.ApiResponse<>(true, "Balance retrieved", balance)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(new DTORequest.ErrorResponse(404, "Not Found", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{accountId}/deposit")
    @Operation(summary = "Deposit money", description = "Deposit money into an account")
    @APIResponse(responseCode = "200", description = "Deposit successful", content = @Content(schema = @Schema(implementation = Account.class)))
    @APIResponse(responseCode = "400", description = "Invalid deposit amount or account not founc")
    public Response deposit(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId, @Parameter(description = "Amount to deposit", required = true) @QueryParam("amount") Double amount) {
        try {
            Account account = accountService.deposit(accountId, amount);
            return Response.ok(account).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(400, "Bad Request", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{accountId}/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraw money from an account")
    @APIResponse(responseCode = "200", description = "Withdrawal successful", content = @Content(schema = @Schema(implementation = Account.class)))
    @APIResponse(responseCode = "400", description = "Invalid withdrawal amount, insufficient balance, or account not found")
    public Response withdraw(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId, @Parameter(description = "Amount to withdraw", required = true) @QueryParam("amount") double amount) {
        try {
            Account account = accountService.withdraw(accountId, amount);
            return Response.ok(account).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(400, "Bad Request", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{accountId}")
    @Operation(summary = "Delete account", description = "Delete an account")
    @APIResponse(responseCode = "204", description = "Account deleted successfully")
    @APIResponse(responseCode = "404", description = "Account not found")
    public Response deleteAccount(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId){
        try {
            accountService.deleteAccount(accountId);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }
}
