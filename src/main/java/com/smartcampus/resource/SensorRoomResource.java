package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.repository.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("rooms") // This maps to /api/v1/rooms because of our ApplicationPath 
public class SensorRoomResource {

    // 1. GET /api/v1/rooms - List all rooms
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getAllRooms() {
        return new ArrayList<>(DataStore.getRooms().values());
    }

    // 2. POST /api/v1/rooms - Create a new room
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        // Basic validation: ensure the room has an ID
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room ID is required").build();
        }
        
        // Save to our in-memory "database"
        DataStore.getRooms().put(room.getId(), room);
        
        // Return 201 Created status
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // 3. GET /api/v1/rooms/{roomId} - Get specific room metadata
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(room).build();
    }
    
    // 4. DELETE /api/v1/rooms/{roomId} - Delete a room
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRooms().get(roomId);
        
        // If the room doesn't exist, return 404 Not Found
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // Business Logic Constraint: Cannot delete if sensors are attached
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            // Throws our custom exception which will be caught by the RoomNotEmptyExceptionMapper
            throw new RoomNotEmptyException("Room cannot be deleted because it contains active sensors.");
        }
        
        // If the room is empty, it is safe to delete
        DataStore.getRooms().remove(roomId);
        
        // 204 No Content is the industry standard for a successful DELETE
        return Response.noContent().build(); 
    }
}