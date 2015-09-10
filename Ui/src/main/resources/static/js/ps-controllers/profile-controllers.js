function profileCtrl ($scope, $rootScope) {

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
}

angular
    .module('provesoft')
    .controller('profileCtrl', profileCtrl);