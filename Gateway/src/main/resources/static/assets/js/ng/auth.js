angular.module('auth', ['ngRoute', 'ngCookies']).config(function($httpProvider) {

	$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

})

.controller('loginCtrl', function($scope, $http, $window, $location) {

        /* Perform check on query parameters if redirect needed */
        var checkForParams = $location.absUrl().split('?');
        if (checkForParams.length > 1) {
            var type = checkForParams[1].split('=')[0];
            if (typeof type !== 'undefined') {

                var token = checkForParams[1].split('=')[1];

                // Password recovery
                if (type === 'r') {
                    $location.url('/passReset?t=' + token);
                }

                // New registration via token
                else if (type === 'n') {
                    $location.url('/tReg?t=' + token);
                }

                // Redirect to registration
                else if (type === 's') {
                    $location.url('/register');
                }
            }
        }

        /* ----------- Initialize variables ----------- */
        $scope.userLoggedin = false;

        $scope.credentials = {};

        // Check if user is logged in
        $scope.checkIfUserLoggedIn = function(callback) {
            $http.get('/user')
                .success(function(data) {
                    if (typeof data.name !== 'undefined') {
                        callback(true);
                    }
                    else {
                        callback(false);
                    }
                })
                .error(function() {
                    callback(false);
                });
        };
        $scope.checkIfUserLoggedIn(function(value) {
            if (value) {
                $window.location.href = '/ui/';
            }
        });

        // Redirect to UI if user is logged in
        $scope.redirectToUi = function() {
            $window.location.href = '/ui/';
        };

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
                    return callback(true);
                }
                else {
                    return callback(false);
                }
            }).error(function() {
                return callback(false);
            });

        };

        $scope.login = function() {

            authenticate($scope.credentials, function(authenticated) {
                if (authenticated) {
                    $window.location.href = '/ui/'
                }
                else {
                    $scope.loginFailed = true;
                }
            }, function(error) {
                $scope.loginFailed = true;
            });
        };
    })


.controller('passReqCtrl', function($scope, $http, $location) {

        $scope.passReset = {
            email: ''
        };

        $scope.processingEmail = false;

        $scope.passResetReq = function() {
            if ( (typeof $scope.passReset.email !== 'undefined') && ($scope.passReset.email !== '') ) {
                $scope.processingEmail = true;
                $http.post('/pr', {
                    email: $scope.passReset.email,
                    url: $location.absUrl()
                }).success(function(data) {
                    $scope.passReset.email = '';
                    $scope.processingEmail = false;
                    $location.url('/');
                }).error(function(error) {
                    $scope.passReset.email = '';
                    $scope.processingEmail = false;
                });
            }
        };
    })


.controller('registerCtrl', function($scope, $http, $window) {

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

        $scope.tab = 1;


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

            // Upper and lower case letters, numbers, and spaces
            var alphaNumSpaceRegex = /^[a-zA-Z0-9 ]+$/i;

            // Email format
            var emailRegex = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;

            // Min 8 characters
            // 1 Uppercase
            // 1 Lowercase
            // 1 Number
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
            $scope.reg.companyName = $scope.reg.companyName.trim();
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
            else if ( !alphaNumSpaceRegex.test($scope.reg.companyName) ) {
                $scope.reg.validationErrors.companyName = 'Company name can only contain letters, numbers and spaces';
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

        };

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
                    return callback(true);
                }
                else {
                    return callback(false);
                }
            }).error(function() {
                return callback(false);
            });

        };

        $scope.login = function() {

            authenticate($scope.credentials, function(authenticated) {
                if (authenticated) {
                    $window.location.href = '/ui/'
                }
                else {
                    $scope.loginFailed = true;
                }
            }, function(error) {
                $scope.loginFailed = true;
            });
        };
    })


.controller('passResetCtrl', function($scope, $http, $location) {

        $scope.passResetVerificationInput = {
            email: '',
            newPassword: '',
            verifyNewPassword: '',
            token: '',
            validationErrors: {}
        };
        $scope.successfullyReset = false;

        // Extract reset token from query parameter
        var param = $location.absUrl().split('?');
        if (param.length > 1) {
            $scope.passResetVerificationInput.token = param[1].split('=')[1];
        }
        else {
            $location.url('/');
        }

        $scope.validatePassword = function() {

            $scope.passResetVerificationInput.validationErrors = {};

            var validationFailed = false;

            // Email format
            var emailRegex = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;

            // Min 8 characters
            // 1 Uppercase
            // 1 Lowercase
            // 1 Number
            var passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/;

            var email = $scope.passResetVerificationInput.email;
            var password = $scope.passResetVerificationInput.newPassword;
            var verifyPassword = $scope.passResetVerificationInput.verifyNewPassword;

            if (email.length > 254) {
                $scope.passResetVerificationInput.validationErrors.email = 'Please enter a valid email of less than 250 characters';
                validationFailed = true;
            }
            else if (email === '') {
                $scope.passResetVerificationInput.validationErrors.email = 'Please enter an email';
                validationFailed = true;
            }
            else if (!emailRegex.test(email)) {
                $scope.passResetVerificationInput.validationErrors.email = 'Please enter a valid email';
                validationFailed = true;
            }

            if (password.length > 254) {
                $scope.passResetVerificationInput.validationErrors.password = 'Please limit password to 255 characters';
                validationFailed = true;
            }
            else if (password === '') {
                $scope.passResetVerificationInput.validationErrors.password = 'Please enter a password';
                validationFailed = true;
            }
            else if ( !passwordRegex.test(password) ) {
                $scope.passResetVerificationInput.validationErrors.password = 'Password must contain at least 8 characters long, 1 uppercase letter, 1 lowercase letter and 1 number';
                validationFailed = true;
            }
            else if (password !== verifyPassword) {
                $scope.passResetVerificationInput.validationErrors.password = 'Passwords do not match';
                $scope.passResetVerificationInput.validationErrors.verifyPassword = ' ';
                validationFailed = true;
            }

            return !validationFailed;
        };

        $scope.recover = function() {
            if ($scope.validatePassword()) {
                $http.post('ps', {
                    email: $scope.passResetVerificationInput.email,
                    password: $scope.passResetVerificationInput.newPassword,
                    token: $scope.passResetVerificationInput.token
                }).success(function(data) {
                    $scope.successfullyReset = true;
                }).error(function(error) {
                });
            }
        };
    })


.controller('tRegCtrl', function($scope, $http, $location) {

        $scope.regVerificationInput = {
            email: '',
            newPassword: '',
            verifyNewPassword: '',
            token: '',
            validationErrors: {}
        };

        $scope.successfullyRegistered = false;

        // Extract registration token from query parameter
        var param = $location.absUrl().split('?');
        if (param.length > 1) {
            $scope.passResetVerificationInput.token = param[1].split('=')[1];
        }
        else {
            $location.url('/');
        }

        $scope.registerByToken = function() {
            if ($scope.validatePassword()) {
                $http.post('tokenReg', {
                    email: $scope.regVerificationInput.email,
                    password: $scope.regVerificationInput.newPassword,
                    token: $scope.regVerificationInput.token
                }).success(function(data) {
                    $scope.successfullyRegistered = true;
                    $location.url('/');
                }).error(function(error) {
                });
            }
        };
    })


.config(['$routeProvider', '$locationProvider',
        function ($routeProvider, $locationProvider) {

            $routeProvider

                .when('/', {
                    templateUrl: 'views/login.html',
                    controller: 'loginCtrl'
                })

                .when('/register', {
                    templateUrl: 'views/register.html',
                    controller: 'registerCtrl'
                })

                .when('/passReq', {
                    templateUrl: 'views/passReq.html',
                    controller: 'passReqCtrl'
                })

                .when('/passReset', {
                    templateUrl: 'views/passReset.html',
                    controller: 'passResetCtrl'
                })

                .when('/tReg', {
                    templateUrl: 'views/tReg.html',
                    controller: 'tRegCtrl'
                })

                .otherwise({redirectTo: '/'});

            $locationProvider.html5Mode(true);
        }
]);
