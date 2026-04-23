package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider // Crucial: Registers this globally
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    
    // We still want to see the real error in our NetBeans console!
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the actual, detailed error internally for developers to debug
        LOGGER.log(Level.SEVERE, "An unexpected internal error occurred", exception);

        // Return a generic, safe error message to the external client
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // HTTP 500
                .type(MediaType.APPLICATION_JSON)
                .entity("{\"error\": \"An unexpected internal server error occurred. Please try again later.\"}")
                .build();
    }
}