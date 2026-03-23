package com.bank.config;

import com.bank.dto.DTORequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;


@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {


    //catches error for the system, it will override the existing error and return a custom error. Though, each requests has error handling
    @Override
    public Response toResponse (Exception exception) {
        if (exception instanceof WebApplicationException wae) {
            return wae.getResponse();
        }

        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new DTORequest.ErrorResponse(400, "Bad Request", exception.getMessage()))
                    .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new DTORequest.ErrorResponse(500, "Internal Server Error", "An unexpected error occurred"))
                .build();
    }
}
