package com.bank.resource;

import com.bank.dto.DTORequest;
import com.bank.entity.User;
import com.bank.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;


//handles the API endpoint and receives requests related to user management such as getting user
// details and deleting user accounts - admin only
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Tag(name="Users (Admin access)", description = "User management endpoint - admin only")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Operation(summary = "Get all users - admin", description = "Get all registered users with their accounts")
    @APIResponse(responseCode = "200", description = "Users retrieved")
    @SecurityRequirement(name = "jwt")
    public Response getAllUser() {
        try {
            List<DTORequest.UserResponse> users = userService.getAllUsers()
                    .stream()
                    .map(userService::toUserResponse)
                    .toList();
            return Response.ok(users).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{userId}")
    @Operation(summary = "Get user by ID - admin", description = "Get user details by user ID")
    @APIResponse(responseCode = "200", description = "User retrieved")
    @APIResponse(responseCode = "404", description = "User not found")
    @SecurityRequirement(name = "jwt")
    public Response getUserbyId(@Parameter(description = "User ID", required = true) @PathParam("userId") Long userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) return Response.ok(userService.toUserResponse(user.get())).build();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new DTORequest.ErrorResponse(404, "Not Found", "User not found"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{userId}")
    @Operation(summary = "Delete user profile - admin", description = "Delete user account permanently by user ID")
    @APIResponse(responseCode = "204", description = "User deleted")
    @APIResponse(responseCode = "404", description = "User not found")
    @SecurityRequirement(name = "jwt")
    public Response deleteUser(@Parameter(description = "User ID", required = true) @PathParam("userId") Long userId) {
        try {
             userService.deleteUser(userId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(new DTORequest.ErrorResponse(400, "Not Found", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }
}

