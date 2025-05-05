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