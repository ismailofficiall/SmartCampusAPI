package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList; 
import java.util.List;      

@Path("sensors")
public class SensorResource {

    // POST /api/v1/sensors - Register a new sensor
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerSensor(Sensor sensor) {
        
        // 1. Dependency Validation: Does the referenced room exist?
        Room parentRoom = DataStore.getRooms().get(sensor.getRoomId());
        
        if (parentRoom == null) {
            // Throws our custom exception which will be caught by the LinkedResourceNotFoundExceptionMapper (422)
            throw new LinkedResourceNotFoundException("Cannot create sensor. Room ID '" + sensor.getRoomId() + "' does not exist.");
        }

        // 2. Save the sensor to the database
        DataStore.getSensors().put(sensor.getId(), sensor);
        
        // 3. Link the sensor back to the room's list of sensors
        parentRoom.getSensorIds().add(sensor.getId());

        // Return 201 Created
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // GET /api/v1/sensors?type={type} - List sensors with optional filtering
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensors(@QueryParam("type") String type) {
        // Grab all sensors from the database
        List<Sensor> allSensors = new ArrayList<>(DataStore.getSensors().values());
        
        // If the client didn't provide a type, just return everything
        if (type == null || type.trim().isEmpty()) {
            return Response.ok(allSensors).build();
        }
        
        // If they did provide a type, filter the list
        List<Sensor> filteredSensors = new ArrayList<>();
        for (Sensor sensor : allSensors) {
            if (sensor.getType() != null && sensor.getType().equalsIgnoreCase(type)) {
                filteredSensors.add(sensor);
            }
        }
        
        return Response.ok(filteredSensors).build();
    }
    
    // Sub-Resource Locator for Historical Data
    // Notice there is NO @GET or @POST annotation here!
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}