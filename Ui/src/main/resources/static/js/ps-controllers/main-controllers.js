'use strict';

function MainCtrl($scope, $rootScope, $window, authService) {

     // ---------- Authentication ----------- //

    authService.getUserAuth.get(function(data) {
        if (data.userName) {
            $rootScope.authenticated = true;
            $rootScope.user = data;

        } else {
            $rootScope.authenticated = false;
        }
    }, function(error) {
           $rootScope.authenticated = false;
    });

    $scope.logout = function() {
         authService.logout.save(function(data) {
             $rootScope.authenticated = false;
             $window.location.href = '/';
         }, function(error) {
            $rootScope.authenticated = false;
            $window.location.href = '/';
         });
    };

     // --------------------------------------- //

};

angular
    .module('provesoft')
    .controller('MainCtrl', MainCtrl);