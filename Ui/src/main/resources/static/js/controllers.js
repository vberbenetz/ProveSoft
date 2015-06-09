/**
 * INSPINIA - Responsive Admin Theme
 * Copyright 2015 Webapplayers.com
 *
 */

/**
 * MainCtrl - controller
 */
function MainCtrl($scope, $http, $window) {

    // ---------- Authentication ----------- //
    $http.get('user').success(function(data) {
        if (data.name) {
            $scope.authenticated = true;
            $scope.user = data.name;
            $http.get('/resource/').success(function(data) {
                $scope.greeting = data;
            })
        } else {
            $scope.authenticated = false;
        }
    }).error(function() {
        $scope.authenticated = false;
    });

    $scope.logout = function() {
        $http.post('logout', {}).success(function() {
            $scope.authenticated = false;
            $window.location.href = '/';
        }).error(function(data) {
            console.log("Logout failed");
            $scope.authenticated = false;
            $window.location.href = '/';
        });
    };
    // --------------------------------------- //


    this.userName = 'Example user';
    this.helloText = 'Welcome in SeedProject';
    this.descriptionText = 'It is an application skeleton for a typical AngularJS web app. You can use it to quickly bootstrap your angular webapp projects and dev environment for these projects.';

};


angular
    .module('inspinia')
    .controller('MainCtrl', MainCtrl)