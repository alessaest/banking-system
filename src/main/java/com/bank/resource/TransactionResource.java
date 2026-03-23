package com.bank.resource;

import com.bank.dto.DTORequest;
import com.bank.service.TransactionService;
import io.quarkus.security.Authenticated;
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


//handles the API endpoint and receives requests related to transactions such as transfer, deposit and transaction history
@Path("/transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Transactions", description = "Transaction management endpoints that requires JWT")
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    @POST
    @Path("/transfer")
    @Operation(summary = "Transfer money", description = "Transfer money from your account to any account")
    @APIResponse(responseCode = "201", description = "Transfer completed successfully")
    @APIResponse(responseCode = "400", description = "Invalid transfer request or insufficient balance")
    @SecurityRequirement(name = "jwt")
    public Response transfer(DTORequest.TransferRequest request, @Context SecurityContext securityContext) {
        try {
            Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
            DTORequest.TransactionResponse tx = transactionService.transferMoney(request, userId);

            return Response.status(Response.Status.CREATED).entity(tx).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DTORequest.ErrorResponse(400, "Transfer Failed", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/deposit")
    @Operation(summary = "Deposit money", description = "Deposit money into your account")
    @APIResponse(responseCode = "201", description = "Deposit transaction completed")
    @APIResponse(responseCode = "400", description = "Invalid deposit request")
    @SecurityRequirement(name = "jwt")
    public Response deposit(DTORequest.DepositRequest request, @Context SecurityContext securityContext) {
        try {
            Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
            DTORequest.TransactionResponse tx = transactionService.deposit(request, userId);

            return Response.status(Response.Status.CREATED).entity(tx).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DTORequest.ErrorResponse(400, "Deposit Failed", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/account/{accountId}/history")
    @Operation(summary = "Get transaction history", description = "Returns all transactions with transactionId of your account")
    @APIResponse(responseCode = "200", description = "History retrieved")
    @APIResponse(responseCode = "400", description = "Invalid account ID or unauthorized access")
    @SecurityRequirement(name = "jwt")
    public Response getAccountHistory(@Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId, @Context SecurityContext securityContext) {
        try {
            Long userId = Long.parseLong(securityContext.getUserPrincipal().getName());
            List<DTORequest.TransactionResponse> history = transactionService.getAccountTransactionHistory(accountId,userId);

            return Response.ok(history).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{transactionId}")
    public Response getTransaction(@Parameter(description = "Transaction ID", required = true) @PathParam("transactionId") Long transactionId) {
        try {
            return transactionService.getTransactionById(transactionId)
                    .map(tx -> Response.ok(tx).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND).entity(new DTORequest.ErrorResponse(404, "Not Found", "Transaction not found")).build());
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }
}
