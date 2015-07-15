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

function documentCreationCtrl($scope, $rootScope, $window, documentCreationService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Initialize file upload (dropzone)
    $scope.files = [];
    $scope.submitClicked = false;

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
        documentCreationService.document.save($scope.newDocument, function(data, status, headers, config) {
            $scope.newDocument = {
                title: '',
                documentType: {},
                organization: {}
            }
        }, function(data, status, headers, config) {

        });
    };

};

function documentRevisionCtrl($scope, $rootScope, $window, $stateParams, documentRevisionService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Keep track of form progress
    $scope.reviseDocumentForm = 1;
    $scope.documentId = $stateParams.documentId;
}

angular
    .module('provesoft')
    .controller('documentLookupCtrl', documentLookupCtrl)
    .controller('documentCreationCtrl', documentCreationCtrl)
    .controller('documentRevisionCtrl', documentRevisionCtrl);