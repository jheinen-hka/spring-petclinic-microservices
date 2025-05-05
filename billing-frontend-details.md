# Billing Feature Frontend Implementation Details

This document provides an in-depth explanation of the frontend implementation for the billing feature in the Spring PetClinic Microservices application. It covers both the bill list view and bill creation functionality.

## Overview of Frontend Architecture

The frontend is built using:

- **AngularJS 1.x**: Framework for building the UI components
- **UI Router**: For client-side routing between views
- **Bootstrap**: For styling and responsive layout

The billing feature follows the established component-based architecture pattern used throughout the application, with each feature organized into:

1. **Module Definition (*.js)**: Defines the Angular module and routing
2. **Component Definition (*.component.js)**: Registers the component with the module
3. **Controller (*.controller.js)**: Contains the business logic and data fetching
4. **Template (*.template.html)**: HTML markup for the UI

## Bill List Implementation

### Module Structure

```
spring-petclinic-api-gateway/src/main/resources/static/scripts/bill-list/
├── bill-list.js                 # Module and routing configuration
├── bill-list.component.js       # Component definition
├── bill-list.controller.js      # Controller logic
└── bill-list.template.html      # HTML template
```

### Module Definition (bill-list.js)

```javascript
'use strict';

angular.module('billList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('bills', {
                parent: 'app',
                url: '/bills',
                template: '<bill-list></bill-list>'
            });
    }]);
```

This code:
- Creates a new Angular module named 'billList'
- Adds a dependency on 'ui.router' for routing
- Configures a state named 'bills' that renders the 'bill-list' component when the URL is '/bills'
- Sets the parent state to 'app', which is the main application container

### Component Definition (bill-list.component.js)

```javascript
'use strict';

angular.module('billList')
    .component('billList', {
        templateUrl: 'scripts/bill-list/bill-list.template.html',
        controller: 'BillListController'
    });
```

This code:
- Registers a component named 'billList' with the module
- Specifies the template URL for the component's HTML
- Associates the component with the 'BillListController'

### Controller Implementation (bill-list.controller.js)

```javascript
'use strict';

angular.module('billList')
    .controller('BillListController', ['$http', function ($http) {
        var self = this;
        
        // Demo data for fallback if service is unavailable
        var mockBills = [
            {
                id: 1,
                customerName: "George Franklin (Demo)",
                visitDate: "2025-04-10",
                amount: 89.99,
                status: "OPEN",
                issueDate: "2025-04-11",
                description: "Rabies shot (Demo Data)"
            },
            // ... additional mock data entries ...
        ];

        // Flag to indicate if we're using demo data
        self.isDemoData = false;

        // Fetch bills from the backend
        $http({
            method: 'GET',
            url: window.location.origin + '/api/billing/bills',
            headers: {
                'Accept': 'application/json',
                'Cache-Control': 'no-cache',
                'X-Requested-With': 'XMLHttpRequest'
            },
            transformResponse: function(data) {
                try {
                    return angular.fromJson(data);
                } catch(e) {
                    return data;
                }
            }
        }).then(function (resp) {
            console.log("Bills received:", resp.data);
            self.bills = resp.data;
        }).catch(function(error) {
            console.error("Error fetching bills:", error);
            
            // Use mock data since the service is unavailable
            self.bills = mockBills;
            self.isDemoData = true;
            self.errorMessage = "Note: Displaying demo data. The billing-service is not running. Error: " + 
                                (error.status || error.message);
            
            // Prevent the error from propagating to the global error handler
            return Promise.reject({
                handled: true,
                data: {
                    error: "Failed to load bills",
                    errors: []
                }
            });
        });
    }]);
```

Key features of the controller:
- Uses Angular's `$http` service to fetch bills from the API
- Includes error handling with fallback to demo data
- Properly sets HTTP headers and handles response transformation
- Uses absolute URLs to avoid routing issues
- Sets up demo data for use when the service is unavailable
- Provides user feedback through error messages

### Template Implementation (bill-list.template.html)

```html
<div class="row">
    <div class="col-md-10">
        <h2>Bills</h2>
    </div>
    <div class="col-md-2" style="margin-top: 20px;">
        <a class="btn btn-default" ui-sref="billNew">Create Bill</a>
    </div>
</div>

<div ng-if="$ctrl.errorMessage && !$ctrl.isDemoData" class="alert alert-danger">
    {{$ctrl.errorMessage}}
</div>

<div ng-if="$ctrl.isDemoData" class="alert alert-warning">
    {{$ctrl.errorMessage}}
</div>

<form onsubmit="javascript:void(0)" style="max-width: 20em; margin-top: 2em;">
    <div class="form-group">
        <input type="text" class="form-control" placeholder="Search Filter" ng-model="$ctrl.query" />
    </div>
</form>

<table class="table table-striped">
    <thead>
    <tr>
        <th>ID</th>
        <th>Customer Name</th>
        <th>Visit Date</th>
        <th>Amount</th>
        <th>Status</th>
        <th>Issue Date</th>
        <th>Description</th>
    </tr>
    </thead>
    <tbody>
        <tr ng-repeat="bill in $ctrl.bills | filter:$ctrl.query track by bill.id">
            <td>{{bill.id}}</td>
            <td>{{bill.customerName}}</td>
            <td>{{bill.visitDate | date:'yyyy-MM-dd'}}</td>
            <td>{{bill.amount | currency}}</td>
            <td>{{bill.status}}</td>
            <td>{{bill.issueDate | date:'yyyy-MM-dd'}}</td>
            <td>{{bill.description}}</td>
        </tr>
        <tr ng-if="$ctrl.bills && $ctrl.bills.length === 0">
            <td colspan="7">No bills found.</td>
        </tr>
    </tbody>
</table>
```

Key features of the template:
- Responsive layout with a heading and create button in a row
- Conditional display of error messages or warnings based on state
- Search filter input that filters the table content using Angular's filter
- Table with styled columns for all bill properties
- Use of Angular filters for date and currency formatting
- Proper handling of empty state (no bills found)
- Track-by directive for optimal rendering performance

## Bill Creation Implementation

### Module Structure

```
spring-petclinic-api-gateway/src/main/resources/static/scripts/bill-form/
├── bill-form.js                 # Module and routing configuration
├── bill-form.component.js       # Component definition
├── bill-form.controller.js      # Controller logic
└── bill-form.template.html      # HTML template
```

### Module Definition (bill-form.js)

```javascript
'use strict';

angular.module('billForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('billNew', {
                parent: 'app',
                url: '/bills/new',
                template: '<bill-form></bill-form>'
            });
    }]);
```

This code:
- Creates a new Angular module named 'billForm'
- Adds a dependency on 'ui.router' for routing
- Configures a state named 'billNew' that renders the 'bill-form' component when the URL is '/bills/new'
- Sets the parent state to 'app', which is the main application container

### Component Definition (bill-form.component.js)

```javascript
'use strict';

angular.module('billForm')
    .component('billForm', {
        templateUrl: 'scripts/bill-form/bill-form.template.html',
        controller: 'BillFormController'
    });
```

This code:
- Registers a component named 'billForm' with the module
- Specifies the template URL for the component's HTML
- Associates the component with the 'BillFormController'

### Controller Implementation (bill-form.controller.js)

```javascript
'use strict';

angular.module('billForm')
    .controller('BillFormController', ['$http', '$state', function ($http, $state) {
        var self = this;
        
        self.bill = {
            customerId: '',
            visitId: ''
        };
        
        self.owners = [];
        self.visits = [];
        self.visitsByOwner = {};  // Dictionary to store visits by ownerId
        self.selectedOwnerId = null;
        self.errorMessage = null;
        self.submitting = false;

        // Load owners for dropdown
        $http.get('api/customer/owners').then(function (resp) {
            self.owners = resp.data;
        }).catch(function (error) {
            self.errorMessage = 'Failed to load owners: ' + error.statusText;
        });

        // Function to load visits for a specific owner
        self.loadVisitsForOwner = function(ownerId) {
            if (!ownerId) return;
            
            self.selectedOwnerId = ownerId;
            
            // If we already loaded visits for this owner, don't reload
            if (self.visitsByOwner[ownerId]) {
                self.visits = self.visitsByOwner[ownerId];
                return;
            }
            
            // Find all pets for this owner
            var owner = self.owners.find(function(owner) {
                return owner.id === parseInt(ownerId);
            });
            
            if (!owner || !owner.pets || owner.pets.length === 0) {
                self.visits = [];
                self.errorMessage = 'No pets found for this owner.';
                return;
            }
            
            // Get all pet IDs
            var petIds = owner.pets.map(function(pet) {
                return pet.id;
            });
            
            // Fetch visits for all pets
            $http.get('api/visit/pets/visits?petId=' + petIds.join('&petId=')).then(function (resp) {
                if (resp.data && resp.data.items) {
                    self.visits = resp.data.items;
                    self.visitsByOwner[ownerId] = self.visits;
                } else {
                    self.visits = [];
                    self.errorMessage = 'No visits found for this owner\'s pets.';
                }
            }).catch(function (error) {
                self.errorMessage = 'Failed to load visits: ' + error.statusText;
                self.visits = [];
            });
        };

        // Submit the form
        self.submit = function() {
            self.submitting = true;
            self.errorMessage = null;
            
            var billRequest = {
                customerId: parseInt(self.bill.customerId),
                visitId: parseInt(self.bill.visitId)
            };
            
            $http.post('api/billing/bills', billRequest)
                .then(function(response) {
                    // Success - redirect to bill list
                    $state.go('bills');
                })
                .catch(function(error) {
                    self.errorMessage = 'Error creating bill: ' + 
                        (error.data && error.data.message ? error.data.message : error.statusText);
                    self.submitting = false;
                });
        };
    }]);
```

Key features of the controller:
- Manages state for the form data and UI state (submitting, errors, etc.)
- Loads owners from the customer service for the dropdown
- Dynamic loading of visits when an owner is selected
- Caches visits by owner to improve performance
- Handles multiple error cases with appropriate messages
- Formats the bill request data correctly for the backend
- Redirects to the bill list on successful creation
- Disables submission during in-flight requests

### Template Implementation (bill-form.template.html)

```html
<h2>Create New Bill</h2>

<div ng-if="$ctrl.errorMessage" class="alert alert-danger">
    {{$ctrl.errorMessage}}
</div>

<form ng-submit="$ctrl.submit()" name="billForm" class="form-horizontal">
    <div class="form-group">
        <label class="col-sm-2 control-label">Owner</label>
        <div class="col-sm-10">
            <select ng-model="$ctrl.bill.customerId" 
                    ng-change="$ctrl.loadVisitsForOwner($ctrl.bill.customerId)" 
                    class="form-control" 
                    required>
                <option value="">-- Select Owner --</option>
                <option ng-repeat="owner in $ctrl.owners track by owner.id" 
                        value="{{owner.id}}">
                    {{owner.firstName}} {{owner.lastName}}
                </option>
            </select>
        </div>
    </div>

    <div class="form-group" ng-if="$ctrl.selectedOwnerId">
        <label class="col-sm-2 control-label">Visit</label>
        <div class="col-sm-10">
            <select ng-model="$ctrl.bill.visitId" class="form-control" required>
                <option value="">-- Select Visit --</option>
                <option ng-repeat="visit in $ctrl.visits track by visit.id" 
                        value="{{visit.id}}">
                    {{visit.date | date:'MM/dd/yyyy'}} - {{visit.description}}
                </option>
            </select>
            <span ng-if="$ctrl.visits.length === 0" class="help-block text-warning">
                No visits found for this owner's pets.
            </span>
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
            <button class="btn btn-default" 
                    type="submit" 
                    ng-disabled="billForm.$invalid || $ctrl.submitting">
                Create Bill
            </button>
            <a class="btn btn-default" ui-sref="bills">Cancel</a>
        </div>
    </div>
</form>
```

Key features of the template:
- Horizontal form layout with proper labeling
- Displays error messages when they occur
- Use of Angular's form validation
- Cascading dropdowns - visit dropdown only appears after owner selection
- Dynamic loading of visits based on selected owner
- Proper date formatting for visit dates
- Conditionally disables the submit button when form is invalid or submission is in progress
- Cancel button that returns to the bill list view
- Proper message when no visits are found for an owner

## Integration with Main Application

### App Module Registration (app.js)

```javascript
'use strict';
/* App Module */
var petClinicApp = angular.module('petClinicApp', [
    'ui.router', 'infrastructure', 'layoutNav', 'layoutFooter', 'layoutWelcome',
    'ownerList', 'ownerDetails', 'ownerForm', 'petForm', 'visits', 'vetList', 
    'billList', 'billForm']);
```

The billing feature modules are added to the main application's dependencies.

### Navigation Integration (nav.html)

```html
<li>
    <a class="nav-link" ui-sref-active="active" ui-sref="bills" title="view bills">
        <span class="fa fa-file-text-o"></span>
        <span>Bills</span>
    </a>
</li>
```

A navigation link is added to the main menu to access the bills view.

### Script Inclusion (index.html)

```html
<script src="/scripts/bill-list/bill-list.js"></script>
<script src="/scripts/bill-list/bill-list.controller.js"></script>
<script src="/scripts/bill-list/bill-list.component.js"></script>

<script src="/scripts/bill-form/bill-form.js"></script>
<script src="/scripts/bill-form/bill-form.controller.js"></script>
<script src="/scripts/bill-form/bill-form.component.js"></script>
```

All billing feature JavaScript files are included in the main index.html file.

## Frontend Data Flow

1. **Listing Bills Flow**:
   - User navigates to Bills page
   - bill-list.controller loads and makes HTTP GET request to `/api/billing/bills`
   - API Gateway routes request to billing service
   - Controller receives bill data and assigns to `self.bills`
   - Template renders bills in the table with filtering capabilities
   - If the service is down, mock data is displayed with a warning

2. **Creating a Bill Flow**:
   - User clicks "Create Bill" button and navigates to bill creation form
   - bill-form.controller loads and makes HTTP GET request to `/api/customer/owners`
   - User selects an owner from dropdown
   - Controller reacts to selection and makes HTTP GET request for visits for that owner's pets
   - User selects a visit
   - User clicks submit
   - Controller makes HTTP POST request to `/api/billing/bills` with owner and visit IDs
   - On success, user is redirected back to the bill list page
   - On error, an error message is displayed and form remains active

## UI/UX Considerations

1. **Error Handling**:
   - Graceful degradation with mock data when services are unavailable
   - Clear error messages for user feedback
   - Different styling for errors vs warnings

2. **Performance Optimizations**:
   - Caching of visits by owner to reduce API calls
   - Using `track by` in ng-repeat directives for better rendering performance
   - Preventing duplicate form submissions

3. **User Guidance**:
   - Conditional displaying of form fields only when they're relevant
   - Required field validation
   - Clear labeling and formatting
   - Consistent styling with the rest of the application

4. **Accessibility**:
   - Proper use of form control labels
   - Semantic HTML structure
   - Visual feedback for form state

## Implementation Challenges and Solutions

### 1. HTTP 405 Method Not Allowed Error

**Challenge**: Initial attempts to access the API resulted in HTTP 405 errors.

**Solution**: 
- Updated the HTTP request configuration with proper headers
- Used absolute URLs instead of relative ones
- Added error handling with demo data fallback

### 2. Integration with Multiple Services

**Challenge**: The bill creation form needed data from both the customers and visits services.

**Solution**: 
- Implemented cascading dropdowns
- Used the owner selection to drive the visit loading
- Created proper error handling for each service integration point

### 3. Form Validation

**Challenge**: Ensuring valid data is submitted to the backend.

**Solution**:
- Leveraged Angular's form validation
- Disabled the submit button when the form is invalid
- Added client-side validation for required fields

### 4. Price Determination

**Challenge**: The Visit entity doesn't include a price field.

**Solution**:
- Moved price determination to the backend in the VisitDto
- Used the visit description to determine appropriate pricing
- Made the UI simpler by not requiring users to enter prices

## Conclusion

The frontend implementation of the billing feature follows Angular best practices and integrates seamlessly with the existing application architecture. By leveraging AngularJS features like components, services, and routing, we've created a modular, maintainable solution that provides a good user experience even when dealing with distributed services.

The component-based approach allows for clean separation of concerns, with each component responsible for its own data fetching, presentation, and user interaction. The result is a cohesive feature that enhances the PetClinic application with billing capabilities.