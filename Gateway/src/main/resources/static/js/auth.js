angular.module('auth', []).config(function($httpProvider) {

	$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

}).controller('navigation',

function($scope, $http, $window) {

    /* ----------- Initialize variables ----------- */
    $scope.tab = 1;

    $scope.credentials = {};

    $scope.reg = {
        firstName: '',
        lastName: '',
        email: '',
        emailVerify: '',
        companyName: '',
        title: '',
        password: '',
        passwordVerify: '',

        validationErrors: {},

        planSelection: 'free'
    };

    $scope.successfullyRegistered = false;


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


    $scope.register = function() {

        $http.post('register', {
            firstName: $scope.reg.firstName,
            lastName: $scope.reg.lastName,
            email: $scope.reg.email,
            companyName: $scope.reg.companyName,
            title: $scope.reg.title,
            password: $scope.reg.password,
            plan: $scope.reg.planSelection
        }).success(function(newUser) {
            $scope.credentials = {
                username: $scope.reg.email,
                password: $scope.reg.password
            };
            $scope.login();
        }).error(function(error) {
        });

    };

    $scope.validateUserInfo = function() {

        // Reset validation objects
        var validationFailed = false;
        var validateEmailOrCompanyFailed = false;
        $scope.reg.validationErrors = {};

        // Upper and lower case letters
        var alphaRegex = /^[a-zA-Z]+$/i;

        // Email format
        var emailRegex = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;

        // Min 8 characters
        // 1 Uppercase
        // 1 Lowercase
        // 1 Number
        // 1 Special Character
        var passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/;

        // Check if email is valid
        if (typeof $scope.reg.email === 'undefined') {
            $scope.reg.validationErrors.email = 'Please enter your email';
            validationFailed = true;
            validateEmailOrCompanyFailed = true;
        }
        else if ( ($scope.reg.email.length == 0) || ($scope.reg.email === '') ) {
            $scope.reg.validationErrors.email = 'Please enter your email';
            validationFailed = true;
            validateEmailOrCompanyFailed = true;
        }
        else if ( $scope.reg.email.length > 254 ) {
            $scope.reg.validationErrors.email = 'Email is invalid because it is too long';
            validationFailed = true;
            validateEmailOrCompanyFailed = true;
        }
        else if ( !emailRegex.test($scope.reg.email) ) {
            $scope.reg.validationErrors.email = 'Please enter a valid email';
            validationFailed = true;
            validateEmailOrCompanyFailed = true;
        }
        else if ($scope.reg.email !== $scope.reg.emailVerify) {
            $scope.reg.validationErrors.email = 'Emails do not match';
            $scope.reg.validationErrors.emailVerify = '';
            validationFailed = true;
            validateEmailOrCompanyFailed = true;
        }

        // Check if company is valid
        if (typeof $scope.reg.companyName === 'undefined') {
            $scope.reg.validationErrors.companyName = 'Please enter your company name';
            validationFailed = true;
            validateEmailOrCompanyFailed = true;
        }
        else if ( ($scope.reg.companyName.length == 0) || ($scope.reg.companyName === '') ) {
            $scope.reg.validationErrors.companyName = 'Please enter your company name';
            validationFailed = true;
            validateEmailOrCompanyFailed = true;
        }
        else if ( $scope.reg.companyName.length > 254 ) {
            $scope.reg.validationErrors.companyName = 'Company name is invalid because it is too long. Please abbreviate';
            validationFailed = true;
            validateEmailOrCompanyFailed = true;
        }

        // First name validation
        if (typeof $scope.reg.firstName === 'undefined') {
            $scope.reg.validationErrors.firstName = 'Please enter your first name';
            validationFailed = true;
        }
        else if ( ($scope.reg.firstName.length == 0) || ($scope.reg.firstName === '') ) {
            $scope.reg.validationErrors.firstName = 'Please enter your first name';
            validationFailed = true;
        }
        else if ( $scope.reg.firstName.length > 254 ) {
            $scope.reg.validationErrors.firstName = 'First name is too long. Please enter a valid first name';
            validationFailed = true;
        }
        else if ( !alphaRegex.test($scope.reg.firstName) ) {
            $scope.reg.validationErrors.firstName = 'Please use only letters for your first name';
            validationFailed = true;
        }

        // Last name validation
        if (typeof $scope.reg.lastName === 'undefined') {
            $scope.reg.validationErrors.lastName = 'Please enter your last name';
            validationFailed = true;
        }
        else if ( ($scope.reg.lastName.length == 0) || ($scope.reg.lastName === '') ) {
            $scope.reg.validationErrors.lastName = 'Please enter your last name';
            validationFailed = true;
        }
        else if ( !alphaRegex.test($scope.reg.lastName) ) {
            $scope.reg.validationErrors.lastName = 'Please use only letters for your last name';
            validationFailed = true;
        }
        else if ( $scope.reg.lastName.length > 254 ) {
            $scope.reg.validationErrors.lastName = 'Last name is too long. Please enter a valid last name';
            validationFailed = true;
        }

        // Title validation
        if (typeof $scope.reg.title === 'undefined') {
            $scope.reg.validationErrors.title = 'Please enter your title';
            validationFailed = true;
        }
        else if ( ($scope.reg.title.length == 0) || ($scope.reg.title === '') ) {
            $scope.reg.validationErrors.title = 'Please enter your title';
            validationFailed = true;
        }
        else if ( $scope.reg.title.length > 254 ) {
            $scope.reg.validationErrors.title = 'Title is too long. Please enter an abbreviated version';
            validationFailed = true;
        }

        // Password validation
        if (typeof $scope.reg.password === 'undefined') {
            $scope.reg.validationErrors.password = 'Please enter your password';
            validationFailed = true;
        }
        else if ( ($scope.reg.password.length == 0) || ($scope.reg.password === '') ) {
            $scope.reg.validationErrors.password = 'Please enter your password';
            validationFailed = true;
        }
        else if ( $scope.reg.password.length > 254 ) {
            $scope.reg.validationErrors.password = 'Please limit password to 250 characters';
            validationFailed = true;
        }
        else if ( !passwordRegex.test($scope.reg.password) ) {
            $scope.reg.validationErrors.password = 'Password must contain at least 8 characters long, 1 uppercase letter, 1 lowercase letter and 1 number';
            validationFailed = true;
        }
        else if ($scope.reg.password !== $scope.reg.passwordVerify) {
            $scope.reg.validationErrors.password = 'Passwords do not match';
            $scope.reg.validationErrors.passwordVerify = '';
            validationFailed = true;
        }

        // Check if email and/or company exist
        if (!validateEmailOrCompanyFailed) {
            $http({
                url: '/check',
                method: 'GET',
                params: {
                    companyName: $scope.reg.companyName,
                    email: $scope.reg.email
                }
            }).success(function(result) {

                // Company exists validation
                if (result.companyExists) {
                    $scope.reg.validationErrors.companyName = 'Company already exists';
                    validationFailed = true;
                }

                // Email exists validation
                if (result.emailExists) {
                    $scope.reg.validationErrors.email = 'Email already in use';
                    validationFailed = true;
                }

                // Advance to next tab if validated
                if (!validationFailed) {
                    $scope.tab = 2;
                }

            }).error(function(error) {
                $scope.error = error;
            });
        }

    }



















});
