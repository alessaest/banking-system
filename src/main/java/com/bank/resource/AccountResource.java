package com.bank.resource;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.entity.User;
import com.bank.service.AccountService;
import com.bank.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;


//handles the API endpoints and receives requests related to account
@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Accounts", description = "Account Management endpoints")
public class AccountResource {

    //constants
    private static final String INTERNAL_SERVER_ERROR_MSG = "Internal Server Error";
    private static final String BAD_REQUEST_MSG = "Bad Request";
    private static final String NOT_FOUND_MSG = "Not Found";

    private final AccountService accountService;
    private final UserService userService;

    AccountResource(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }


    @GET
    @Path("/my_accounts")
    @Operation(summary = "Get my accounts", description = "Get all accounts of the authenticated user")
    @SecurityRequirement(name = "jwt")
    public Response getMyAccounts(@Context SecurityContext securityContext) {
        try {
            Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
            List<DTORequest.AccountResponse> accounts = accountService.getMyAccounts(userId)
                    .stream()
                    .map(accountService::toAccountResponse)
                    .toList();
            return Response.ok(accounts).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, INTERNAL_SERVER_ERROR_MSG, e.getMessage())).build();
        }
    }

    @POST
    @Path("/create_savings")
    @Authenticated
    public DTORequest.AccountResponse createSavingsAccount (
            @Context SecurityContext context,
            DTORequest.CreateSavingsRequest request) {
        Long userId = Long.parseLong(context.getUserPrincipal().getName());
        User user = userService.getUserById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        Account account = accountService.createSavingsAccount(
                user, request.initialBalance, 2.5
        );
        return accountService.toAccountResponse(account);
    }

    @GET
    @Path("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Get the current balance of your account")
    @APIResponse(responseCode = "404", description = "Account not found")
    @SecurityRequirement(name = "jwt")
    public Response getBalance(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId, @Context SecurityContext securityContext) {
        try {
            Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
            Double balance = accountService.getAccountBalance(accountId, userId);
            return Response.ok(new DTORequest.ApiResponse<>(true, "Balance retrieved", balance)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(400, BAD_REQUEST_MSG, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, INTERNAL_SERVER_ERROR_MSG, e.getMessage())).build();
        }
    }

    @POST
    @Path("/{accountId}/deposit_todebit")
    @Operation(summary = "Deposit to DEBIT account", description = "Deposit money into your DEBIT account (unlimited)")
    @APIResponse(responseCode = "200", description = "Deposit successful")
    @APIResponse(responseCode = "400", description = "Invalid deposit request or account is not DEBIT")
    @SecurityRequirement(name = "jwt")
    public Response depositToDebit(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId,
                                   @Parameter(description = "Amount to deposit", required = true) @QueryParam("amount") Double amount,
                                   @Context SecurityContext securityContext) {
        try {
            Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
            DTORequest.TransactionResponse tx = accountService.depositToDebit(accountId, amount, userId, false);
            return Response.ok(tx).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DTORequest.ErrorResponse(400, BAD_REQUEST_MSG, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, INTERNAL_SERVER_ERROR_MSG, e.getMessage())).build();
        }
    }

    @POST
    @Path("/{accountId}/deposit_tocredit")
    @Operation(summary = "Deposit to CREDIT account", description = "Deposit money into your CREDIT account (respects credit limit)")
    @APIResponse(responseCode = "200", description = "Deposit successful")
    @APIResponse(responseCode = "400", description = "Invalid deposit request, credit limit exceeded, or account is not CREDIT")
    @SecurityRequirement(name = "jwt")
    public Response depositToCredit(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId,
                                    @Parameter(description = "Amount to deposit", required = true) @QueryParam("amount") Double amount,
                                    @Context SecurityContext securityContext) {
        try {
            Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
            DTORequest.TransactionResponse tx = accountService.depositToCredit(accountId, amount, userId, false);
            return Response.ok(tx).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DTORequest.ErrorResponse(400, BAD_REQUEST_MSG, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, INTERNAL_SERVER_ERROR_MSG, e.getMessage())).build();
        }
    }

    @POST
    @Path("/{accountId}/deposit_savings")
    @Authenticated
    public DTORequest.TransactionResponse depositToSavings(
            @PathParam("accountId") Long accountId,
            DTORequest.DepositRequest request,
            @Context SecurityContext context) {
        Long userId = Long.parseLong(context.getUserPrincipal().getName());
        return accountService.depositToSavings(accountId, request.getAmount(), userId, false);
    }



    @POST
    @Path("/{accountId}/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraw money from your account")
    @APIResponse(responseCode = "200", description = "Withdrawal successful")
    @APIResponse(responseCode = "400", description = "Invalid amount request")
    @SecurityRequirement(name = "jwt")
    public Response withdraw(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId, @Parameter(description = "Amount to withdraw", required = true) @QueryParam("amount") Double amount, @Context SecurityContext securityContext) {
        try {
            Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
            DTORequest.TransactionResponse tx = accountService.withdraw(accountId, amount, userId);
            return Response.ok(tx).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(400, BAD_REQUEST_MSG, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, INTERNAL_SERVER_ERROR_MSG, e.getMessage())).build();
        }
    }

    //admin access
    @DELETE
    @Path("/{accountId}")
    @RolesAllowed("admin")
    @Operation(summary = "Delete account", description = "Delete an account by ID (Admin only)")
    @APIResponse(responseCode = "204", description = "Account deleted")
    @APIResponse(responseCode = "404", description = "Account not found")
    @SecurityRequirement(name = "jwt")
    public Response deleteAccount(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId) {
        try {
            accountService.deleteAccount(accountId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(new DTORequest.ErrorResponse(404, NOT_FOUND_MSG, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, INTERNAL_SERVER_ERROR_MSG, e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{accountId}/balance")
    @RolesAllowed("admin")
    @Operation(summary = "Update Credit balance", description = "Update the balance of a credit account (Admin only)")
    @APIResponse(responseCode = "200", description = "Balance updated")
    @APIResponse(responseCode = "400", description = "Invalid balance update request")
    @SecurityRequirement(name = "jwt")
    public Response updateCreditBalance(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId, DTORequest.UpdateCreditBalanceRequest request) {
        try {
            DTORequest.AccountResponse account =
                    accountService.updateCreditBalance(accountId, request.getBalance());
            return Response.ok(account).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(404, BAD_REQUEST_MSG, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, INTERNAL_SERVER_ERROR_MSG, e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{accountId}/credit")
    @RolesAllowed("admin")
    @Operation(summary = "Update Credit Account", description = "Update the credit limit and/or balance of a CREDIT account (Admin only)")
    @APIResponse(responseCode = "200", description = "Credit account updated")
    @APIResponse(responseCode = "400", description = "Invalid credit update request")
    @SecurityRequirement(name = "jwt")
    public Response updateCredit(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId,
                                      DTORequest.UpdateCreditBalanceRequest request) {
        try {
            DTORequest.AccountResponse account =
                    accountService.updateCreditLimit(accountId, request.getAmount());
            return Response.ok(account).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DTORequest.ErrorResponse(400, BAD_REQUEST_MSG, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, INTERNAL_SERVER_ERROR_MSG, e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{accountId}/interest_rate")
    @RolesAllowed("admin")
    public Response updateInterestRate(
            @PathParam("accountId") Long accountId,
            DTORequest.UpdateInterestRateRequest request) {
        try {
            DTORequest.AccountResponse account =
                    accountService.updateSavingsInterestRate(accountId, request.getInterestRate());
            return Response.ok(account).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DTORequest.ErrorResponse(400, BAD_REQUEST_MSG, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, INTERNAL_SERVER_ERROR_MSG, e.getMessage())).build();
        }
    }


    //manually trigger the interest calculation
    //1.2.0
    @POST
    @Path("/admin/apply_interest")
    @RolesAllowed("admin")
    public DTORequest.ApiResponse<Double> triggerMonthlyInterest(@Context SecurityContext context) {
        accountService.applyMonthlyInterestToAllSavings();
        return new DTORequest.ApiResponse<>(true, "Monthly interest applied to all savings accounts", null);
    }

}