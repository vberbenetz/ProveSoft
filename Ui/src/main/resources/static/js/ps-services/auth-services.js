'use strict';

function authService($resource) {
    return {
        getUserAuth: $resource('/resource/user/auth'),

        logout: $resource('logout')
    }
};

angular
    .module('provesoft')
    .factory('authService', authService);