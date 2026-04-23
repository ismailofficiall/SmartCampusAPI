package com.smartcampus.config;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiMetadata() {
        // Create the basic metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "v1");
        metadata.put("contact", "admin@smartcampus.ac.uk");
        
        // Create the hypermedia links as required by the brief
        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        
        metadata.put("_links", links);

        return Response.ok(metadata).build();
    }
}