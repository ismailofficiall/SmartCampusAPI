package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.repository.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class SensorReadingResource {

    private String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        List<SensorReading> readings = DataStore.getSensorReadings().getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading newReading) {
        Sensor parentSensor = DataStore.getSensors().get(sensorId);
        
        if (parentSensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\": \"Sensor not found.\"}")
                           .build();
        }

        // --- NEW BUSINESS LOGIC: Block if sensor is in maintenance ---
        if ("MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException("Sensor '" + sensorId + "' is currently in maintenance and cannot accept new readings.");
        }

        if (newReading.getId() == null) {
            newReading = new SensorReading(newReading.getValue());
        }

        DataStore.getSensorReadings().putIfAbsent(sensorId, new ArrayList<>());
        DataStore.getSensorReadings().get(sensorId).add(newReading);

        parentSensor.setCurrentValue(newReading.getValue());

        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }
}