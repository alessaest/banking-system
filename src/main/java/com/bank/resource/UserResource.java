package com.bank.resource;

import com.bank.dto.DTORequest;
import com.bank.entity.User;
import com.bank.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name="Users", description = "User management endpoint")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Path("/{userId}")
    @Operation(summary = "Get user details by ID")
    @APIResponse(responseCode = "200", description = "User found")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response getUser(@PathParam("userId") Long userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) return Response.ok(user.get()).build();
            return Response.status(Response.Status.NOT_FOUND).entity(new DTORequest.ErrorResponse(404, "Not Found", "User not found")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @GET
    @Operation (summary = "Get all users for admin")
    public Response getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return Response.ok(users).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{userId}")
    @Operation(summary = "Update user profile")
    @APIResponse(responseCode = "200", description = "User updated successfully")
    @APIResponse(responseCode = "400", description = "Invalid input data")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response updateUser(@Parameter(description = "User ID", required = true) @PathParam("userId") Long userId, DTORequest.RegisterRequest request) {
        try {
            User updatedUser = userService.updateUserProfile(userId, request);
            return Response.ok(updatedUser).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new DTORequest.ErrorResponse(400, "Bad Request", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{userId}")
    @Operation(summary = "Delete user account")
    @APIResponse(responseCode = "204", description = "User deleted successfully")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response deleteUser(@Parameter(description = "User ID", required = true) @PathParam("userId") Long userId) {
        try {
            userService.deleteUser(userId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(new DTORequest.ErrorResponse(404, "Not Found", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new DTORequest.ErrorResponse(500, "Internal Server Error", e.getMessage())).build();
        }
    }
}

