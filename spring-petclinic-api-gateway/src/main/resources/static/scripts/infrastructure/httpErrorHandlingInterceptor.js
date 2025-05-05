'use strict';

/**
 * Global HTTP errors handler.
 */
angular.module('infrastructure')
    .factory('HttpErrorHandlingInterceptor', function () {
        return {
            responseError: function (response) {
                var error = response.data;
                var errorMessage = error.error || 'An error occurred';
                
                if (error.errors && Array.isArray(error.errors)) {
                    errorMessage += "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n");
                }
                
                alert(errorMessage);
                return response;
            }
        }
    });
