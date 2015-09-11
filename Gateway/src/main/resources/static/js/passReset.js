angular.module('passReset', []).config(function($httpProvider) {

    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

}).controller('ps',

function($scope, $http, $location, $timeout) {

    $scope.input = {
        email: '',
        token: ''
    };

    $scope.successfullyReset = false;

    var token = $location.absUrl().split('?')[1];
    if (token.split('=')[0] == 'ps') {
        $scope.token = token.split('=')[1];
    }

    $scope.recover = function() {
        if ( ($scope.email !== '') && ($scope.token !== '') ) {
            $http.post('ps', {
                email: $scope.input.email,
                token: $scope.input.token
            }).success(function(data) {
                $scope.successfullyReset = true;
            }).error(function(error) {
            });
        }
    };

    $scope.flashNotification = function() {
        $timeout(function() {
            $scope.successfullyReset = false;
        }, 2000);
    };

    $scope.$watch('successfullyReset', function(newVal, oldVal) {
        if (newVal !== oldVal) {
            if (newVal) {
                $scope.flashNotification();
            }
        }
    });


});
