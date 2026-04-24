# 🏛️ Smart Campus API

> A highly robust, thread-safe RESTful web service built with **Jakarta EE (JAX-RS)** for managing university infrastructure, including thousands of rooms and hardware sensors (CO2 monitors, temperature sensors, etc.).

---

## 🛠️ Technology Stack

| Component | Technology Used |
|---|---|
| **Language** | Java 11+ (Tested on JDK 21) |
| **Framework** | Jakarta RESTful Web Services (JAX-RS) |
| **Application Server** | GlassFish Server 7.x |
| **Build Tool** | Apache Maven |
| **Data Storage** | In-Memory (`ConcurrentHashMap`) |
| **Architecture** | REST, Sub-Resource Locators, HATEOAS |

---

## 📂 Project Architecture

The application enforces a strict separation of concerns, utilizing global exception mappers and JAX-RS filters to keep resource controllers clean and focused strictly on business logic.

```text
src/main/java/com/smartcampus/
├── config/
│   ├── SmartCampusApplication.java        ← JAX-RS Application Base Path (/api/v1)
│   └── LoggingFilter.java                 ← Global API Request/Response Interceptor
├── model/
│   ├── Room.java                          ← Room Entity POJO
│   ├── Sensor.java                        ← Sensor Entity POJO
│   └── SensorReading.java                 ← Historical Reading POJO
├── repository/
│   └── DataStore.java                     ← Thread-safe Singleton Database
├── resource/
│   ├── DiscoveryResource.java             ← API Root / HATEOAS Links
│   ├── SensorRoomResource.java            ← Room Management Endpoints
│   ├── SensorResource.java                ← Sensor Management Endpoints
│   └── SensorReadingResource.java         ← Delegated Sub-Resource Locator
└── exception/
    ├── GlobalExceptionMapper.java         ← HTTP 500 Catch-all 
    ├── RoomNotEmptyExceptionMapper.java   ← HTTP 409 Conflict
    ├── SensorUnavailableException...      ← HTTP 403 Forbidden
    └── LinkedResourceNotFound...          ← HTTP 422 Unprocessable Entity

---

HTTP Method,Endpoint,Description,Status Code
GET,/api/v1/,API Discovery & Hypermedia Links,200 OK
GET,/api/v1/rooms,Retrieve all registered rooms,200 OK
POST,/api/v1/rooms,Register a new room,201 Created
GET,/api/v1/rooms/{id},Retrieve specific room metadata,200 OK
DELETE,/api/v1/rooms/{id},Delete a room (Blocked if sensors attached),204 No Content
GET,/api/v1/sensors,Retrieve sensors (Supports ?type= filtering),200 OK
POST,/api/v1/sensors,Register a new sensor linked to a room,201 Created
GET,/api/v1/sensors/{id}/readings,Sub-Resource: Get reading history,200 OK
POST,/api/v1/sensors/{id}/readings,Sub-Resource: Add a new reading,201 Created

---

## Build and Launch Instructions

### Prerequisites
* **Java Development Kit (JDK):** Version 11 or higher (Tested on JDK 21).
* **Maven:** Installed and configured in your system PATH.
* **Application Server:** GlassFish Server 7.x (required for Jakarta EE 10 support).

### Step-by-Step Setup
1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/](https://github.com/)[Your-Username]/SmartCampusAPI.git
   cd SmartCampusAPI
   ```

2. **Clean and Build the Project:**
   Use Maven to download the required Jakarta dependencies and compile the `.war` file.
   ```bash
   mvn clean install
   ```

3. **Deploy to GlassFish:**
   * Open your IDE (e.g., Apache NetBeans).
   * Link the project to your GlassFish 7 server instance.
   * Run the project. The server will deploy the application to `http://localhost:8080/SmartCampusAPI/api/v1/`.

---

## Sample API Interactions (cURL Commands)

Ensure the server is running before executing these commands.

**1. API Discovery (GET)**
Retrieve the API metadata and hypermedia navigation links.
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/
```

**2. Create a New Room (POST)**
Registers a new room in the in-memory data store.
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d "{\"id\": \"LIB-301\", \"name\": \"Quiet Study\", \"capacity\": 50}"
```

**3. Register a Sensor (POST)**
Registers a new CO2 sensor linked to the room created above.
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d "{\"id\": \"CO2-001\", \"type\": \"CO2\", \"status\": \"ACTIVE\", \"currentValue\": 400.5, \"roomId\": \"LIB-301\"}"
```

**4. Filter Sensors by Type (GET)**
Retrieves a list of sensors, filtered specifically by the "CO2" type.
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

**5. Add a Sensor Reading via Sub-Resource (POST)**
Posts a new historical reading to a specific sensor, which automatically updates the sensor's parent object.
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings \
     -H "Content-Type: application/json" \
     -d "{\"value\": 415.2}"
```

---

## Conceptual Report

### Part 1: Service Architecture & Setup
**1. Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures.**
> By default, JAX-RS follows a "per-request" lifecycle. A new instance of a Resource class is created to handle each incoming HTTP request, and it is subsequently garbage collected once the response is sent. Because we are strictly using in-memory data structures instead of a database, this lifecycle means standard instance variables would be wiped out after every request. Therefore, our in-memory data structures must be declared as `static` to persist across the application's lifespan. Furthermore, to prevent data loss or race conditions when multiple requests read or modify the data simultaneously, we must manage synchronization by utilizing thread-safe collections, specifically `ConcurrentHashMap`.

**2. Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**
> HATEOAS (Hypermedia as the Engine of Application State) decoupling the client from the server's specific URL structure. By including navigation links within the API responses, the API becomes self-discoverable. This benefits client developers because they no longer need to hardcode endpoint paths based on static documentation. Instead, the client application can dynamically navigate the API by following the provided links, making the system highly resilient to future backend routing changes.

### Part 2: Room Management
**3. When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.**
> Returning only IDs minimizes network bandwidth consumption, which is critical for mobile networks or large-scale systems. However, this forces the client to make separate, additional API calls to fetch details for each room, increasing latency. Returning full room objects provides all necessary data in a single request, reducing round-trips but increasing the initial payload size and the amount of client-side processing required to parse the larger JSON structure.

**4. Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**
> Yes, the `DELETE` operation is idempotent. If a client sends a `DELETE` request for "LIB-301", the server successfully removes the room and returns a `204 No Content` status. If the client mistakenly sends the exact same `DELETE` request again, the server will not find the room and will return a `404 Not Found` status. Although the HTTP response codes differ, the underlying state of the server remains exactly the same: the room "LIB-301" is gone. Because subsequent identical requests do not corrupt the data store, the operation is idempotent.

### Part 3: Sensor Operations & Linking
**5. We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**
> If a client attempts to send data in a format like `text/plain` to an endpoint annotated strictly with `@Consumes(MediaType.APPLICATION_JSON)`, the JAX-RS runtime framework intercepts the request before it reaches the Java method. It evaluates the `Content-Type` header, recognizes the mismatch, and automatically aborts the operation, returning an HTTP `415 Unsupported Media Type` error. This protects the server from attempting to deserialize invalid payloads and prevents runtime parsing exceptions.

**6. You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?**
> In REST, Path Parameters (`/sensors/{id}`) identify specific resources or hierarchical relationships, while Query Parameters (`?type=CO2`) modify the representation of a collection. The query parameter approach is superior for filtering because it is optional and combinable. If a client later wants to filter by multiple attributes (e.g., `?type=CO2&status=ACTIVE`), query parameters handle this gracefully. Stacking multiple optional filters into a URL path would result in deeply nested, brittle, and unmanageable routing configurations.

### Part 4: Deep Nesting with Sub-Resources
**7. Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?**
> The Sub-Resource Locator pattern enforces the Single Responsibility Principle. If every nested path were defined in a single controller class, it would quickly bloat into a difficult-to-maintain "God class." By delegating logic to separate sub-resource classes (like `SensorReadingResource`), the API becomes highly modular. Each class is cohesive and focused solely on its domain entity, which reduces merge conflicts, improves code readability, and allows for easier horizontal scaling among development teams.

### Part 5: Advanced Error Handling, Exception Mapping & Logging
**8. Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**
> A standard `404 Not Found` implies that the target URI itself does not exist. However, when posting a new sensor, the URI is valid, and the JSON syntax is perfectly correct. The failure occurs because a piece of data *inside* the payload (the foreign key `roomId`) references an entity that cannot be processed. `422 Unprocessable Entity` is semantically accurate because it tells the client that the server understands the content type and syntax, but was unable to process the instructions due to semantic reference errors.

**9. From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**
> Exposing internal Java stack traces causes "Information Leakage". It provides an attacker with a blueprint of the server's internal architecture. An attacker can gather highly specific information, including the exact names and versions of frameworks in use, internal file paths, and custom class names. With this information, an attacker can search for known vulnerabilities (CVEs) associated with those exact library versions or craft highly targeted injection attacks based on the exposed structural logic.

**10. Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**
> Using JAX-RS filters leverages Aspect-Oriented Programming (AOP). Manually inserting `Logger.info()` into every resource method violates the DRY (Don't Repeat Yourself) principle and tightly couples business logic with infrastructural code. By using a JAX-RS `@Provider` filter, we centralize the logging logic into a single class that automatically wraps all HTTP traffic. This ensures consistent observability across the entire API while keeping the resource classes clean and focused strictly on their business rules.
