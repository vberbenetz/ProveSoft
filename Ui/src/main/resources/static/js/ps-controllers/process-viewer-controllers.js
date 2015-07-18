'use strict';

function documentLookupCtrl($scope, $rootScope, $window, $timeout, documentLookupService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    $scope.searchString = '';
    $scope.prevSearchString = '';
    $scope.documentSearchResults = [];
    $scope.revisions = [];
    $scope.lastFetchedRevisions = '';      // Prevent the same revisions from being fetched again on-click

    $scope.$watch('searchString', function(newVal, oldVal) {

        // Clear search results
        if ($scope.documentSearchResults.length > 0) {
            $scope.documentSearchResults = [];
        }

        // Only perform search if string is greater than 4 characters
        if (newVal.length > 4) {
            $scope.executeSearch();
        }
    });


    /* ---------- Controller Methods ----------- */

    $scope.executeSearch = function() {
        $timeout( function() {

            // Run if search strings are different
            if ($scope.searchString !== $scope.prevSearchString) {
                $scope.prevSearchString = $scope.searchString;

                documentLookupService.lookup.query({searchString: $scope.searchString}, function(data, status, headers, config) {
                    $scope.documentSearchResults = data;
                }, function(data, status, headers, config) {
                    $scope.error = status;
                });
            }
        } , 700);
    };

    $scope.getRevisions = function(documentId) {
        if ($scope.lastFetchedRevisions != documentId) {
            documentLookupService.revision.query({documentId: documentId}, function(revisions) {
                $scope.revisions = revisions;
            }, function(error) {
                $scope.err = error;
            });
            $scope.lastFetchedRevisions = documentId;
        }
    }

};

function documentCreationCtrl($scope, $rootScope, $state, $window, documentCreationService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Initialize file upload (dropzone)
    $scope.files = [];
    $scope.submitClicked = false;
    $scope.uploadedSuccessfully = false;
    $scope.creatingDocument = false;

    // Keep track of form progress
    $scope.newDocumentForm = 1;

    // Initialize data for form
    $scope.documentTypes = [];
    $scope.organizations = [];
    $scope.populatedDocumentTypesDropdown = false;
    $scope.populatedOrganizationsDropdown = false;

    $scope.newDocument = {
        title: '',
        documentType: {},
        organization: {}
    };

    // Get initial data for form
    documentCreationService.documentType.query(function(documentTypes) {
        $scope.documentTypes = documentTypes;
        $scope.loadDocumentTypes();

        documentCreationService.organization.query(function(organizations) {
            $scope.organizations = organizations;
            $scope.loadOrganizations();
        }, function(error) {
            $scope.err = error;
        });

    }, function(error) {
        $scope.err = error;
    });


    // ------------------ Methods ------------------- //

    // Need to load documentTypes twice for chosen dropdown watcher to register collection of options
    $scope.loadDocumentTypes = function() {
        if (!$scope.populatedDocumentTypesDropdown) {
            documentCreationService.documentType.query(function(documentTypes) {
                $scope.populatedDocumentTypesDropdown = true;
                $scope.documentTypes = documentTypes;
            }, function(error) {
                $scope.err = error;
            });
        }
    };

    // Need to load organizations twice for chosen dropdown watcher to register collection of options
    $scope.loadOrganizations = function() {
        if (!$scope.populatedOrganizationsDropdown) {
            documentCreationService.organization.query(function(organizations) {
                $scope.populatedOrganizationsDropdown = true;
                $scope.organizations = organizations;
            }, function(error) {
                $scope.err = error;
            });
        }
    };

    $scope.createNewDocument = function() {
        $scope.creatingDocument = true;

        documentCreationService.document.save($scope.newDocument, function(data, status, headers, config) {

            $scope.documentId = data.id;
            $scope.processDropzone();
            $scope.newDocument = {
                title: '',
                documentType: {},
                organization: {}
            };

        }, function(data, status, headers, config) {
            $scope.err = status;
        });
    };

    $scope.$watch('uploadSuccessful', function(newVal, oldVal) {
        if (newVal == true) {

            // Redirect on successful creation and upload
            $state.go('process-viewer.document-lookup');
        }
    });

}

function documentRevisionCtrl($scope, $rootScope, $window, $state, $stateParams, documentRevisionService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Keep track of form progress
    $scope.reviseDocumentForm = 1;
    $scope.documentId = $stateParams.documentId;

    $scope.docDownloadLink = '/resource/download?documentId=' + $scope.documentId;
    $scope.redlineDocDownloadLink = '/resource/download?documentId=' + $scope.documentId + '&isRedline=true';

    $scope.revision = {
        changeReason: ''
    };

    // Keep track of file uploads
    $scope.uploadSuccessful = false;
    $scope.uploadingDocument = false;
    $scope.uploadedFile = '';


    // ------------------ Methods ------------------- //

    $scope.uploadRevisedDocument = function() {

        // Only upload if it is a new file
        if ($scope.uploadedFile != $scope.file.name) {
            $scope.uploadingDocument = true;
            $scope.processDropzone();
            $scope.uploadedFile = $scope.file.name;
        }
        else {
            $scope.reviseDocumentForm = 2;
        }
    };

    // Watch directive for when upload completes
    $scope.$watch('uploadSuccessful', function(newVal, oldVal) {
        if (newVal == true) {
            $scope.uploadSuccessful = false;        // Reset flag
            $scope.uploadingDocument = false;
            $scope.reviseDocumentForm = 2;
        }
    });

    $scope.addRevision = function() {
        var revisionPayload = {
            documentId: $scope.documentId,
            changeReason: $scope.revision.changeReason,
            changeUserName: $rootScope.user.userName
        };

        documentRevisionService.revision.save(revisionPayload, function(data, status, headers, config) {
            $state.go('process-viewer.document-lookup');
        }, function(data, status, headers, config) {
            $scope.err = status;
        });
    };

}

angular
    .module('provesoft')
    .controller('documentLookupCtrl', documentLookupCtrl)
    .controller('documentCreationCtrl', documentCreationCtrl)
    .controller('documentRevisionCtrl', documentRevisionCtrl);