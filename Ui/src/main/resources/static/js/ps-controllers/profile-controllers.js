function profileCtrl ($scope) {

    // Dropzone element
    $scope.dropzoneConfig = {
        url: '/resource/upload/profilePicture',
        maxFileSize: 10,
        paramName: "uploadfile",
        autoProcessQueue: true
    };
}

angular
    .module('provesoft')
    .controller('profileCtrl', profileCtrl);