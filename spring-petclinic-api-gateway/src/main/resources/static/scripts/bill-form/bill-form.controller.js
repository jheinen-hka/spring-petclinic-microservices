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