package com.bank.resource;

import com.bank.dto.DTORequest;
import com.bank.service.AuthService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;


//handles the API endpoint and receives requests related to authentication
@Path("/auth/register")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthResource {

    private final AuthService authService;

    AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @Path("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticate user with username and password and returns JWT token and account info"
    )
    @APIResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = DTORequest.AuthResponse.class))
    )
    @APIResponse(responseCode = "400", description = "Invalid credentials")
    public Response login(DTORequest.LoginRequest request) {
        try {
            DTORequest.AuthResponse response = authService.login(request);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DTORequest.ErrorResponse(400, "Authentication Failed", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account"
    )
    @APIResponse(
            responseCode = "201",
            description = "Registration successful - JWT token included",
            content = @Content(schema = @Schema(implementation = DTORequest.AuthResponse.class))
    )
    @APIResponse(responseCode = "400", description = "Registration failed - username or email already exists")
    public Response register(DTORequest.RegisterRequest request) {
        try {
            DTORequest.AuthResponse response = authService.register(request);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new DTORequest.ErrorResponse(400, "Registration Failed", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }
}
