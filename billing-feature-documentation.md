# Billing Feature Documentation

## Overview

This document provides a detailed explanation of the billing features implemented in the Spring PetClinic Microservices application. The features include:

1. Bill List View - for displaying all bills in the system
2. Bill Creation - for creating new bills by selecting owners and visits
3. Integration with Customer and Visit services

## Architecture Overview

The billing functionality is built on top of the existing microservices architecture, with the following key components:

1. **Billing Service**: Backend service that handles bill creation, storage, and retrieval
2. **API Gateway**: Routes requests from the frontend to the appropriate microservices
3. **Frontend Components**: AngularJS components for the UI, communicating with the backend via the API Gateway
4. **Integration Points**: The Billing service communicates with the Customers and Visits services to retrieve owner and visit information

## Backend Implementation

### 1. Billing Service

The Billing Service is a Spring Boot application responsible for managing billing data:

#### Key Components:

- **BillController**: REST controller exposing endpoints for bill management
  - `GET /bills` - List all bills
  - `GET /bills/{id}` - Get a specific bill
  - `POST /bills` - Create a new bill
  - `PUT /bills/{id}/status` - Update a bill's status
  - `DELETE /bills/{id}` - Delete a bill

- **BillService**: Business logic for bill operations
  - Integration with Customer and Visit services
  - Error handling for service failures
  - Data conversion and mapping

- **Bill Model**: Core domain model with fields:
  - `id` - Unique identifier
  - `customerId` - Reference to customer/owner
  - `visitId` - Reference to the visit
  - `customerName` - Name for display purposes
  - `visitDate` - Date of the visit
  - `amount` - Cost of the bill
  - `issueDate` - Date the bill was issued
  - `status` - Bill status (OPEN, PAID, CANCELLED)
  - `description` - Description of services provided

### 2. Integration with Other Services

The billing service integrates with:

- **Customers Service**: Retrieves owner details using Feign client
- **Visits Service**: Retrieves visit details using Feign client

#### Integration Challenges Solved:

1. **Missing Visit Endpoint**: Added a `/visits/{id}` endpoint to the Visit service
2. **Missing Price Field**: Enhanced VisitDto to calculate prices based on the visit description
3. **ID Type Mismatch**: Fixed Integer vs Long type conversions between services
4. **Date Format Conversion**: Added conversion between Date and LocalDate types

## Frontend Implementation

### 1. Bill List View

Displays all bills in a tabular format:

#### Components:

- **bill-list.js**: Module and routing configuration
- **bill-list.component.js**: Component definition
- **bill-list.controller.js**: Controller logic for data fetching
- **bill-list.template.html**: HTML template with table and search functionality

#### Features:

- Filtering/searching bills
- Proper date and currency formatting
- Error handling with fallback to demo data
- Link to create new bills

### 2. Bill Creation Form

A form that allows users to create new bills:

#### Components:

- **bill-form.js**: Module and routing configuration
- **bill-form.component.js**: Component definition
- **bill-form.controller.js**: Controller logic for form handling
- **bill-form.template.html**: HTML form template

#### Features:

- Dynamic owner selection with dropdown
- Dynamic visit selection based on selected owner
- Validation and error handling
- Form submission

## API Gateway Configuration

The API Gateway routes requests to the appropriate microservices:

```yaml
# From spring-petclinic-api-gateway/src/main/resources/application.yml
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
- Routes requests to '/api/billing/**' to the billing service
- Strips the first two path segments before forwarding the request

## Data Flow

### Bill List Flow:

1. User navigates to the Bills page
2. Frontend makes GET request to `/api/billing/bills`
3. API Gateway routes the request to the billing service's `/bills` endpoint
4. Billing service returns the list of bills
5. Frontend renders the bills in a table

### Bill Creation Flow:

1. User navigates to the bill creation form
2. Frontend loads owners from customer service
3. User selects an owner, which triggers loading of visits for that owner's pets
4. User selects a visit and submits the form
5. Frontend sends POST request to `/api/billing/bills`
6. API Gateway routes the request to the billing service
7. Billing service:
   - Retrieves owner details from the customers service
   - Retrieves visit details from the visits service
   - Creates a new bill
   - Returns the created bill
8. Frontend redirects to the bill list page showing the new bill

## Testing

To test the implementation, the application needs to be running with all required services:
- Config Server
- Discovery Server
- API Gateway
- Customers Service
- Visits Service
- Billing Service

### Starting the Billing Service

To start the billing service, run the following command from the project root:

```bash
java -jar spring-petclinic-billing-service.jar
```

### Testing the Complete Flow

1. Navigate to the application (typically http://localhost:8080)
2. Click on the "Bills" link in the navigation bar
3. View the list of existing bills
4. Click "Create Bill" button
5. Select an owner from the dropdown
6. Select a visit for that owner
7. Submit the form
8. Verify the new bill appears in the bill list

## Conclusion

This implementation follows the established patterns of the existing application:
- Component-based architecture for the frontend
- RESTful service design for the backend
- Consistent styling and user experience
- Service integration using Spring Cloud

The billing feature is fully integrated into the Spring PetClinic Microservices application and follows the same architectural principles as the rest of the application.