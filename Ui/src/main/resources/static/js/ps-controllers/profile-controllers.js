function profileCtrl ($scope, $rootScope, $timeout, userService) {

    $scope.passReset = {
        oldPassword: '',
        newPassword: '',
        verifyNewPassword: '',
        validationErrors: {}
    };

    $scope.uploadSuccessful = false;

    // Dropzone element
    $scope.dropzoneConfig = {
        url: '/resource/upload/profilePicture',
        maxFileSize: 10,
        paramName: "uploadfile",
        autoProcessQueue: true
    };

    $scope.$watch('uploadSuccessful', function(newVal, oldVal) {
        if (newVal !== oldVal) {
            if (newVal) {
                $rootScope.profilePictureUpdated = true;
                $scope.uploadSuccessful = false;
            }
        }
    });


    /* --------------- Password Reset Procedure --------------------- */

    $scope.resetPassword = function() {
        if ($scope.validateNewPassword()) {
            if ($scope.passReset.newPassword === $scope.passReset.verifyNewPassword) {
                var resetPayload = {
                    oldPassword: $scope.passReset.oldPassword,
                    newPassword: $scope.passReset.newPassword
                };

                userService.pr.resetRequest(resetPayload, function(data, status, headers, config) {
                    $scope.resetSuccessful = true;
                }, function(data) {
                    if (data.data.errorCode === 1) {
                        $scope.passReset.validationErrors.oldPassword = 'Old password is incorrect';
                    }
                    else {
                        $scope.passReset.validationErrors.newPassword = 'Error changing password';
                    }
                });
            }
        }
    };

    $scope.validateNewPassword = function() {

        $scope.passReset.validationErrors = {};

        var validationFailed = false;

        // Min 8 characters
        // 1 Uppercase
        // 1 Lowercase
        // 1 Number
        var passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/;

        var oldPassword = $scope.passReset.oldPassword;
        var password = $scope.passReset.newPassword;
        var verifyPassword = $scope.passReset.verifyNewPassword;

        if ( (oldPassword === '') || (oldPassword.length > 254) ) {
            $scope.passReset.validationErrors.oldPassword = 'Please enter your old password';
            validationFailed = true;
        }

        if (password.length > 254) {
            $scope.passReset.validationErrors.newPassword = 'Please limit password to 250 characters';
            validationFailed = true;
        }
        else if (password === '') {
            $scope.passReset.validationErrors.newPassword = 'Please enter a new password';
            validationFailed = true;
        }
        else if ( !passwordRegex.test(password) ) {
            $scope.passReset.validationErrors.newPassword = 'Password must contain at least 8 characters long, 1 uppercase letter, 1 lowercase letter and 1 number';
            validationFailed = true;
        }
        else if (password !== verifyPassword) {
            $scope.passReset.validationErrors.newPassword = 'Passwords do not match';
            $scope.passReset.validationErrors.verifyNewPassword = ' ';
            validationFailed = true;
        }

        if (verifyPassword === '') {
            $scope.passReset.validationErrors.verifyNewPassword = 'Please enter a new password';
            validationFailed = true;
        }

        return !validationFailed;
    };

    $scope.flashSuccessNotification = function() {
        $timeout(function() {
            $scope.resetSuccessful = false;
        }, 2000);
    };

    $scope.$watch('resetSuccessful', function(newVal, oldVal) {
        if (newVal !== oldVal) {
            if (newVal) {
                $scope.flashSuccessNotification();
            }
        }
    });

}

angular
    .module('provesoft')
    .controller('profileCtrl', profileCtrl);