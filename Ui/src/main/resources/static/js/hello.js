angular.module('hello', []).controller('home',

function($scope, $http) {

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
        }).error(function(data) {
            console.log("Logout failed");
            $scope.authenticated = false;
        });
    }

});
