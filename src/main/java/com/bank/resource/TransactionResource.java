package com.bank.resource;

import com.bank.dto.DTORequest;
import com.bank.entity.Transaction;
import com.bank.service.TransactionService;
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

@Path("/transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    @POST
    @Path("/transfer")
    @Operation(
            summary = "Transfer money",
            description = "Transfer money between two accounts"
    )
    @APIResponse(
            responseCode = "201",
            description = "Transfer completed successfully",
            content = @Content(schema = @Schema(implementation = Transaction.class))
    )
    @APIResponse(responseCode = "400", description = "Invalid transfer request or insufficient balance")
    public Response transferMoney(DTORequest.TransferRequest request) {
        try {
            Transaction transaction = transactionService.transferMoney(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    ""
            );
            return Response.status(Response.Status.CREATED).entity(transaction).build();
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
    @Operation(
            summary = "Deposit money",
            description = "Deposit money into an account"
    )
    @APIResponse(
            responseCode = "201",
            description = "Deposit transaction completed",
            content = @Content(schema = @Schema(implementation = Transaction.class))
    )
    @APIResponse(responseCode = "400", description = "Invalid deposit request")
    public Response deposit(DTORequest.DepositRequest request) {
        try {
            Transaction transaction = transactionService.createDepositTransaction(
                    request.getAccountId(),
                    request.getAmount(),
                    "Deposit"
            );
            return Response.status(Response.Status.CREATED).entity(transaction).build();
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
    @Path("/{transactionId}")
    @Operation(
            summary = "Get transaction details",
            description = "Retrieve details of a specific transaction"
    )
    @APIResponse(
            responseCode = "200",
            description = "Transaction found",
            content = @Content(schema = @Schema(implementation = Transaction.class))
    )
    @APIResponse(responseCode = "404", description = "Transaction not found")
    public Response getTransaction(
            @Parameter(description = "Transaction ID", required = true) @PathParam("transactionId") Long transactionId) {
        try {
            Optional<Transaction> transaction = transactionService.getTransactionById(transactionId);
            if (transaction.isPresent()) {
                return Response.ok(transaction.get()).build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new DTORequest.ErrorResponse(404, "Not Found", "Transaction not found"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/account/{accountId}/history")
    @Operation(
            summary = "Get account transaction history",
            description = "Retrieve all transactions for a specific account"
    )
    @APIResponse(
            responseCode = "200",
            description = "Transaction history retrieved",
            content = @Content(schema = @Schema(implementation = Transaction.class))
    )
    public Response getAccountTransactionHistory(
            @Parameter(description = "Account ID", required = true) @PathParam("accountId") Long accountId) {
        try {
            List<Transaction> transactions = transactionService.getAccountTransactionHistory(accountId);
            return Response.ok(transactions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/user/{userId}/history")
    @Operation(
            summary = "Get user transaction history",
            description = "Retrieve all transactions for a user across all their accounts"
    )
    @APIResponse(
            responseCode = "200",
            description = "User transaction history retrieved",
            content = @Content(schema = @Schema(implementation = Transaction.class))
    )
    public Response getUserTransactionHistory(
            @Parameter(description = "User ID", required = true) @PathParam("userId") Long userId) {
        try {
            List<Transaction> transactions = transactionService.getUserTransactionHistory(userId);
            return Response.ok(transactions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/type/{type}")
    @Operation(
            summary = "Get transactions by type",
            description = "Retrieve transactions filtered by type (TRANSFER, DEPOSIT, WITHDRAWAL)"
    )
    @APIResponse(
            responseCode = "200",
            description = "Transactions retrieved",
            content = @Content(schema = @Schema(implementation = Transaction.class))
    )
    public Response getTransactionsByType(
            @Parameter(description = "Transaction type", required = true) @PathParam("type") String type) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByType(type);
            return Response.ok(transactions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }
}
