'use strict';

angular.module('billList')
    .controller('BillListController', ['$http', function ($http) {
        var self = this;
        
        // Create mock data for demo purposes if the service is not available
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
            {
                id: 2,
                customerName: "Maria Escobito (Demo)",
                visitDate: "2025-04-12",
                amount: 129.50,
                status: "PAID",
                issueDate: "2025-04-13",
                description: "Neutered (Demo Data)"
            },
            {
                id: 3,
                customerName: "David Schroeder (Demo)",
                visitDate: "2025-04-15",
                amount: 45.00,
                status: "CANCELLED",
                issueDate: "2025-04-16",
                description: "Spayed (Demo Data)"
            }
        ];

        // Flag to indicate if we're using demo data
        self.isDemoData = false;

        // Disable caching for this request
        // Use absolute URL instead of relative to avoid potential routing issues
        var config = {
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
        };

        $http(config).then(function (resp) {
            console.log("Bills received:", resp.data);
            self.bills = resp.data;
        }).catch(function(error) {
            console.error("Error fetching bills:", error);
            
            // Use mock data since the service is unavailable
            self.bills = mockBills;
            self.isDemoData = true;
            self.errorMessage = "Note: Displaying demo data. The billing-service is not running. Error: " + (error.status || error.message);
            
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