'use strict';

function documentLookupCtrl($scope, $rootScope, $window) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Keep track of form progress
    $scope.newDocumentForm = 1;

}

angular
    .module('provesoft')
    .controller('documentLookupCtrl', documentLookupCtrl);