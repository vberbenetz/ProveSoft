'use strict';

function manageUsersCtrl($scope, $rootScope, $window, manageUsersService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Set the starting view for the right panel
    $scope.rightPanelView = 'new-user';
    $scope.rightPanelData = {};

    // Load all organizations
    manageUsersService.allOrganizations.query(function(orgs) {

        $scope.organizations = orgs;

        // Load preview of users
        manageUsersService.first10.query(function(userSubset) {

            $scope.usersToDisplay = userSubset;

            // Append organization name to user
            for (var i = 0; i < userSubset.length; i++) {
                for (var j = 0; j < orgs.length; i++) {

                    if (userSubset[i].primaryOrgId === orgs[j].organizationId) {
                        $scope.usersToDisplay[i].primaryOrgName = orgs[j].name;
                        break;
                    }
                }
            }

        }, function(error) {
            $scope.err = error;
        });

    }, function(error) {
        $scope.err = error;
    });

    // ------------------ Methods -------------------- //

    $scope.changeRightPanel = function(view, data) {
        $scope.rightPanelView = view;
        $scope.rightPanelData = data;
    }


};

angular
    .module('provesoft')
    .controller('manageUsersCtrl', manageUsersCtrl);