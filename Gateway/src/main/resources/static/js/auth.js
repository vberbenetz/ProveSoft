angular.module('auth', []).config(function($httpProvider) {

	$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

}).controller('navigation',

function($scope, $http, $window) {

	var authenticate = function(credentials, callback) {

		var headers = credentials ? {
			authorization : "Basic "
					+ btoa(credentials.username + ":"
							+ credentials.password)
		} : {};

		$scope.user = '';
		$http.get('user', {
			headers : headers
		}).success(function(data) {
			if (data.name) {
				$scope.authenticated = true;
				$scope.user = data.name
			} else {
				$scope.authenticated = false;
			}
			callback && callback(true);
		}).error(function() {
			$scope.authenticated = false;
			callback && callback(false);
		});

	};

	authenticate();

	$scope.credentials = {};
	$scope.login = function() {
		authenticate($scope.credentials, function(authenticated) {
			$scope.authenticated = authenticated;
			$scope.error = !authenticated;
            $window.location.href = '/ui/';
		})
	};

	$scope.logout = function() {
		$http.post('logout', {}).success(function() {
			$scope.authenticated = false;
		}).error(function(data) {
			console.log("Logout failed");
			$scope.authenticated = false;
		});
	};

    $scope.reg = {};
    $scope.register = function() {
        $http.post('register', {
            firstName: $scope.reg.firstName,
            lastName: $scope.reg.lastName,
            email: $scope.reg.email,
            companyName: $scope.reg.companyName,
            title: $scope.reg.title,
            password: $scope.reg.password
        }).success(function(newUser) {
            $scope.credentials = {
                username: newUser.username,
                password: newUser.password
            };
            $scope.login();
        }).error(function(error) {
        });
    }

});
