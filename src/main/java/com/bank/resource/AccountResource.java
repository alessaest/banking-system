package com.bank.resource;

import com.bank.dto.DTORequest;
import com.bank.service.AccountService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Accounts", description = "Account Management endpoints")
public class AccountResource {

    @Inject
    AccountService accountService;

    @GET
    @Path("/my-accounts")
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
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
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
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(400, "Bad Request", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{accountId}/deposit")
    @Operation(summary = "Deposit money", description = "Deposit money into your account")
    @APIResponse(responseCode = "200", description = "Deposit successful")
    @APIResponse(responseCode = "400", description = "Invalid deposit request")
    @SecurityRequirement(name = "jwt")
    public Response deposit(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId, @Parameter(description = "Amount to deposit", required = true) @QueryParam("amount") Double amount, @Context SecurityContext securityContext) {
        try {
            Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
            DTORequest.TransactionResponse tx = accountService.deposit(accountId, amount, userId);
            return Response.ok(tx).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(400, "Bad Request", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Request", e.getMessage())).build();
        }
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
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(400, "Bad Request", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Request", e.getMessage())).build();
        }
    }

    //admin
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
            return Response.status(Response.Status.NOT_FOUND).entity(new DTORequest.ErrorResponse(404, "Not Found", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Request", e.getMessage())).build();
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
            DTORequest.AccountResponse account = accountService.updateCreditBalance(accountId, request.getBalance());
            return Response.ok(account).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(404, "Bad Request", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Request", e.getMessage())).build();
        }
    }
}