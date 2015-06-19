'use strict';

function manageUsersService($resource) {
    return {
        allUsers: $resource('/resource/users/all'),

        first10: $resource('/resource/users/first10'),

        allOrganizations: $resource('/resource/organizations/all')
    }
};

angular
    .module('provesoft')
    .factory('manageUsersService', manageUsersService);