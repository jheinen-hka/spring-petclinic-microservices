# Billing Feature Backend Implementation Details

This document provides an in-depth explanation of the backend implementation for the billing feature in the Spring PetClinic Microservices application. It covers the billing service itself as well as the necessary changes to other services to enable proper integration.

## Billing Service Architecture

The Billing Service is built as a Spring Boot microservice within the Spring PetClinic Microservices architecture, following the same patterns and conventions as the other services in the application.

### Core Components

The backend implementation is organized using a standard layered architecture:

1. **Controller Layer**: REST endpoints for client interaction
2. **Service Layer**: Business logic and coordination with other services
3. **Repository Layer**: Data access and persistence
4. **Model Layer**: Domain entities
5. **DTO Layer**: Data transfer objects for service communication
6. **Configuration Layer**: Feign clients for inter-service communication

### Directory Structure

```
spring-petclinic-billing-service/
├── src/main/java/org/springframework/samples/petclinic/billing/
│   ├── BillingServiceApplication.java        # Application bootstrap
│   ├── config/                               # Service clients
│   │   ├── CustomerClient.java               # Feign client for customer service
│   │   └── VisitClient.java                  # Feign client for visit service
│   ├── dto/                                  # Data transfer objects
│   │   ├── CustomerDto.java                  # Owner data from customer service
│   │   └── VisitDto.java                     # Visit data with price calculation
│   ├── model/                                # Domain model
│   │   ├── Bill.java                         # Bill entity
│   │   ├── BillRepository.java               # Data access interface
│   │   └── BillStatus.java                   # Bill status enum
│   └── web/                                  # Web layer
│       ├── BillController.java               # REST endpoints
│       ├── BillRequest.java                  # Request body for bill creation
│       └── BillService.java                  # Business logic
└── src/main/resources/
    ├── application.yml                       # Service configuration
    └── db/                                   # Database scripts
        ├── hsqldb/                           # HSQLDB scripts
        │   ├── data.sql                      # Sample data
        │   └── schema.sql                    # Schema definition
        └── mysql/                            # MySQL scripts
            ├── data.sql                      # Sample data
            └── schema.sql                    # Schema definition
```

## Domain Model Implementation

### Bill Entity (`Bill.java`)

```java
@Entity
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "visit_id", nullable = false)
    private Long visitId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BillStatus status;

    @Column(name = "description")
    private String description;

    // Getters and setters...
}
```

The Bill entity represents a single billing record with properties for:
- Customer information (ID and name)
- Visit information (ID, date, and description)
- Billing details (amount, issue date, and status)

### Bill Status Enum (`BillStatus.java`)

```java
public enum BillStatus {
    OPEN,
    PAID,
    CANCELLED
}
```

This enum defines the possible states of a bill.

### Bill Repository (`BillRepository.java`)

```java
public interface BillRepository extends JpaRepository<Bill, Long> {
    // Spring Data JPA provides standard CRUD operations
    // Custom query methods can be added as needed
}
```

The repository extends Spring Data JPA's `JpaRepository` to provide standard CRUD operations for bills.

## Database Schema Design

### Schema Definition (`schema.sql`)

```sql
CREATE TABLE bills (
  id BIGINT IDENTITY PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  visit_id BIGINT NOT NULL,
  customer_name VARCHAR(255) NOT NULL,
  visit_date DATE,
  amount DECIMAL(10,2),
  issue_date DATE,
  status VARCHAR(20),
  description VARCHAR(255)
);

CREATE INDEX bills_customer_id ON bills (customer_id);
CREATE INDEX bills_visit_id ON bills (visit_id);
```

The schema creates a `bills` table with columns matching the Bill entity and adds indexes for efficient querying by customer ID and visit ID.

### Sample Data (`data.sql`)

```sql
INSERT INTO bills VALUES (1, 1, 2, 'George Franklin', '2025-04-10', 89.99, '2025-04-11', 'OPEN', 'rabies shot');
INSERT INTO bills VALUES (2, 8, 3, 'Maria Escobito', '2025-04-12', 129.50, '2025-04-13', 'PAID', 'neutered');
INSERT INTO bills VALUES (3, 9, 4, 'David Schroeder', '2025-04-15', 45.00, '2025-04-16', 'CANCELLED', 'spayed');
```

Sample data provides initial bills for testing and demonstration purposes.

## Service Layer Implementation

### Bill Service (`BillService.java`)

```java
@Service
public class BillService {
    private final BillRepository billRepository;
    private final CustomerClient customerClient;
    private final VisitClient visitClient;

    public BillService(BillRepository billRepository, CustomerClient customerClient, VisitClient visitClient) {
        this.billRepository = billRepository;
        this.customerClient = customerClient;
        this.visitClient = visitClient;
    }

    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    public Optional<Bill> findById(Long id) {
        return billRepository.findById(id);
    }

    public Bill createBill(Long customerId, Long visitId) {
        CustomerDto customer;
        VisitDto visit;

        try {
            customer = customerClient.getCustomerById(customerId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch customer");
        }

        try {
            // Convert Long visitId to Integer since Visit service uses Integer IDs
            Integer visitIdAsInt = visitId.intValue();
            visit = visitClient.getVisitById(visitIdAsInt);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch visit");
        }

        Bill bill = new Bill();
        bill.setCustomerId(customerId);
        bill.setVisitId(visitId);
        bill.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        bill.setVisitDate(visit.getDate());
        bill.setAmount(visit.getPrice());
        bill.setDescription(visit.getDescription());
        bill.setIssueDate(LocalDate.now());
        bill.setStatus(BillStatus.OPEN);

        return billRepository.save(bill);
    }

    public Bill save(Bill bill) {
        return billRepository.save(bill);
    }

    public void deleteById(Long id) {
        billRepository.deleteById(id);
    }
}
```

Key features of the Bill Service:
- Basic CRUD operations for bills
- Integration with Customer and Visit services via Feign clients
- Error handling for service communication failures
- Data transformation from service DTOs to the Bill entity
- Type conversion between Integer (Visit service) and Long (Billing service)

## REST API Implementation

### Bill Request DTO (`BillRequest.java`)

```java
public class BillRequest {
    @NotNull
    private Long customerId;
    
    @NotNull
    private Long visitId;

    // Default constructor for deserialization
    public BillRequest() {
    }
    
    public BillRequest(Long customerId, Long visitId) {
        this.customerId = customerId;
        this.visitId = visitId;
    }

    // Getters and setters...
}
```

This DTO defines the expected request body for bill creation, with validation annotations.

### Bill Controller (`BillController.java`)

```java
@RestController
@RequestMapping("/bills")
public class BillController {
    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public List<Bill> getAllBills() {
        return billService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBill(@PathVariable Long id) {
        return billService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestBody BillRequest request) {
        Bill bill = billService.createBill(request.getCustomerId(), request.getVisitId());
        return ResponseEntity.created(URI.create("/bills/" + bill.getId())).body(bill);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Bill> updateStatus(@PathVariable Long id, @RequestBody BillStatus status) {
        return billService.findById(id).map(bill -> {
            bill.setStatus(status);
            billService.save(bill);
            return ResponseEntity.ok(bill);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id) {
        billService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

Key features of the Bill Controller:
- RESTful endpoints for all CRUD operations
- Proper HTTP status codes for responses
- Use of `ResponseEntity` for more control over HTTP response
- Parameter validation with `@RequestBody`
- Spring's type conversion for path variables

## Service Integration Implementation

### Customer Service Client (`CustomerClient.java`)

```java
@FeignClient(name = "customers-service")
public interface CustomerClient {
    @GetMapping("/owners/{id}")
    CustomerDto getCustomerById(@PathVariable("id") Long id);
}
```

This Feign client interface provides a declarative way to call the Customers service.

### Visit Service Client (`VisitClient.java`)

```java
@FeignClient(name = "visits-service")
public interface VisitClient {
    @GetMapping("/visits/{id}")
    VisitDto getVisitById(@PathVariable("id") Integer id);
}
```

This Feign client interface calls the Visits service, with the ID parameter as Integer to match the Visit service's ID type.

### Customer DTO (`CustomerDto.java`)

```java
public class CustomerDto {
    private Long id;
    private String firstName;
    private String lastName;

    // Getters and setters...
}
```

This simplified DTO captures the essential customer information needed for billing.

### Visit DTO with Smart Pricing (`VisitDto.java`)

```java
public class VisitDto {
    private Long id;
    // The visit service uses Date, but our billing service uses LocalDate, so we need to handle the conversion
    private Date date;
    private BigDecimal price;
    private String description;
    private Integer petId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        // Convert Date to LocalDate if date exists, otherwise return today's date
        return date != null ? new java.sql.Date(date.getTime()).toLocalDate() : LocalDate.now();
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
        // If price is not set in the Visit service, use a default price based on the description
        if (price == null) {
            // Simple logic to assign a price based on the description
            if (description != null) {
                String lowerDesc = description.toLowerCase();
                if (lowerDesc.contains("rabies")) {
                    return new BigDecimal("89.99");
                } else if (lowerDesc.contains("neuter")) {
                    return new BigDecimal("129.50");
                } else if (lowerDesc.contains("spay")) {
                    return new BigDecimal("149.99");
                } else if (lowerDesc.contains("check") || lowerDesc.contains("exam")) {
                    return new BigDecimal("59.99");
                }
            }
            // Default price if no match
            return new BigDecimal("45.00");
        }
        return price;
    }

    // Other getters and setters...
}
```

Key features of the Visit DTO:
- Automatic date conversion between `Date` (Visit service) and `LocalDate` (Billing service)
- Smart price determination based on visit description
- Default values for missing data

## Modifications to Visit Service

To support the Billing service's requirements, we added a missing endpoint to the Visit service.

### Visit Resource Enhancement (`VisitResource.java`)

```java
/**
 * Read a single visit.
 */
@GetMapping("/visits/{visitId}")
public Visit getVisit(@PathVariable("visitId") @Min(1) int visitId) {
    return visitRepository.findById(visitId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit " + visitId + " not found"));
}
```

This new endpoint allows the Billing service to retrieve a specific visit by ID.

## Service Configuration

### Application Configuration (`application.yml`)

```yaml
spring:
  application:
    name: billing-service
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}

---
spring:
  config:
    activate:
      on-profile: docker
    import: configserver:http://config-server:8888
```

The configuration:
- Sets the application name for service discovery
- Imports configuration from the Config Server
- Provides a separate profile for Docker deployment

## API Gateway Integration

The API Gateway is configured to route requests to the Billing service:

### Gateway Configuration (`application.yml` in API Gateway)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: billing-service
          uri: lb://billing-service
          predicates:
            - Path=/api/billing/**
          filters:
            - StripPrefix=2
```

This configuration:
- Routes requests to the path pattern `/api/billing/**` to the Billing service
- Uses the service registry for load balancing (`lb://billing-service`)
- Strips the `/api/billing` prefix when forwarding requests

## Advanced Implementation Details

### Request Body vs. Request Parameters

We refactored the bill creation endpoint to use a request body instead of request parameters:

Original implementation with request parameters:

```java
@PostMapping
public ResponseEntity<Bill> createBill(@RequestParam Long customerId,
                                      @RequestParam Long visitId) {
    Bill bill = billService.createBill(customerId, visitId);
    return ResponseEntity.created(URI.create("/bills/" + bill.getId())).body(bill);
}
```

Improved implementation with request body:

```java
@PostMapping
public ResponseEntity<Bill> createBill(@RequestBody BillRequest request) {
    Bill bill = billService.createBill(request.getCustomerId(), request.getVisitId());
    return ResponseEntity.created(URI.create("/bills/" + bill.getId())).body(bill);
}
```

Benefits of the request body approach:
- Better aligns with RESTful API design principles
- Allows for validation through bean validation annotations
- More extensible for future additional parameters
- Supports more complex data structures

### Type Handling Between Services

The Visits service uses `Integer` for IDs, while the Billing service uses `Long`. We addressed this mismatch:

```java
// Convert Long visitId to Integer since Visit service uses Integer IDs
Integer visitIdAsInt = visitId.intValue();
visit = visitClient.getVisitById(visitIdAsInt);
```

This explicit conversion:
- Prevents runtime type errors
- Makes the type conversion explicit and obvious
- Allows the two services to maintain their own ID conventions

### Smart Price Calculation

Since the Visits service doesn't include pricing information, we implemented smart price calculation based on the visit description:

```java
public BigDecimal getPrice() {
    // If price is not set in the Visit service, use a default price based on the description
    if (price == null) {
        // Simple logic to assign a price based on the description
        if (description != null) {
            String lowerDesc = description.toLowerCase();
            if (lowerDesc.contains("rabies")) {
                return new BigDecimal("89.99");
            } else if (lowerDesc.contains("neuter")) {
                return new BigDecimal("129.50");
            } else if (lowerDesc.contains("spay")) {
                return new BigDecimal("149.99");
            } else if (lowerDesc.contains("check") || lowerDesc.contains("exam")) {
                return new BigDecimal("59.99");
            }
        }
        // Default price if no match
        return new BigDecimal("45.00");
    }
    return price;
}
```

This approach:
- Avoids modifying the Visit service's database schema
- Keeps price determination logic encapsulated
- Provides reasonable default prices based on the type of procedure
- Makes the UI simpler by not requiring price input

### Error Handling

We implemented comprehensive error handling for service communication:

```java
try {
    customer = customerClient.getCustomerById(customerId);
} catch (FeignException.NotFound e) {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
} catch (FeignException e) {
    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch customer");
}
```

This approach:
- Differentiates between "not found" and other service errors
- Translates internal errors to appropriate HTTP status codes
- Provides meaningful error messages for API consumers
- Leverages Spring's `ResponseStatusException` for clean error responses

## Service Discovery and Registration

The Billing service is registered with the Discovery service through Spring Cloud:

```java
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
public class BillingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}
```

The annotations:
- `@EnableDiscoveryClient`: Registers the service with Eureka
- `@EnableFeignClients`: Enables declarative REST clients
- `@SpringBootApplication`: Bootstrap the Spring Boot application

## Backend Data Flow

1. **Bill List Flow**:
   - API Gateway receives GET request to `/api/billing/bills`
   - Request is routed to the Billing service's `/bills` endpoint
   - `BillController.getAllBills()` delegates to `BillService.findAll()`
   - `BillRepository.findAll()` retrieves all bills from the database
   - Bills are serialized to JSON and returned to the client

2. **Bill Creation Flow**:
   - API Gateway receives POST request to `/api/billing/bills`
   - Request is routed to the Billing service's `/bills` endpoint
   - `BillController.createBill()` extracts parameters from request body
   - `BillService.createBill()` is called with customerId and visitId
   - `CustomerClient.getCustomerById()` fetches owner from Customers service
   - `VisitClient.getVisitById()` fetches visit from Visits service
   - New Bill entity is created with data from both services
   - Bill is saved to database and returned to the client

## Implementation Challenges and Solutions

### 1. Missing Visit Endpoint

**Challenge**: The Visits service didn't have an endpoint to retrieve a visit by ID.

**Solution**: 
- Added a new endpoint `/visits/{visitId}` to the Visit service
- Implemented with proper error handling for not found cases
- Used the same return type as the existing endpoints

### 2. Service ID Type Mismatch

**Challenge**: Visit service uses Integer for IDs, while Billing service uses Long.

**Solution**:
- Added explicit type conversion in the service layer
- Used appropriate types in Feign client interfaces
- Added proper error handling for conversion failures

### 3. Missing Price Field in Visit Entity

**Challenge**: The Visit entity doesn't have a price field, which is needed for billing.

**Solution**:
- Created a smart VisitDto with price determination logic
- Used the visit description to infer reasonable pricing
- Kept the logic in the DTO for encapsulation

### 4. REST API Design Improvement

**Challenge**: The original POST endpoint used request parameters instead of a more RESTful approach.

**Solution**:
- Created a BillRequest DTO for the request body
- Refactored the controller to use @RequestBody
- Added validation annotations for request integrity

## Conclusion

The backend implementation of the billing feature follows the microservices architecture pattern established in the Spring PetClinic application. Through careful integration with existing services, smart data handling, and robust error management, we've created a reliable and maintainable billing system.

The service implements standard REST endpoints, uses Spring Data JPA for persistence, and integrates with other services through Feign clients. The implementation successfully overcomes several integration challenges, including type mismatches, missing endpoints, and data transformations.