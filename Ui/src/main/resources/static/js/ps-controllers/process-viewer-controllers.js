'use strict';

function documentLookupCtrl($scope, $rootScope, $window, $timeout, $modal, documentLookupService, signoffPathsService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    $scope.isRedlineUsed = false;

    $scope.noResultsFound = false;
    $scope.searchString = '';
    $scope.prevSearchString = '';
    $scope.documentSearchResults = [];
    $scope.revisions = [];
    $scope.recentDocumentActivity = [];

    // ng-model for new comment
    $scope.newDocumentComment = '';

    $scope.activeDocument = {};             // Keep track of current active document (right panel timeline)
    $scope.lastFetchedRevisions = '';       // Keep track of which document revisions were last fetched for (documentId)
    $scope.lastFetchedActivity = '';        // Keep track of which document activities were last fetched (documentId)

    // Steps associated with signoff path, used in modal
    $scope.signoffPathSteps = [];
    $scope.prevDocIdStepsLookup = -1;

    // Use this to determine how to sort document results
    /*
        States are:
        ID_A        - Sort by ID ascending
        ID_D        - Sort by ID descending
        TITLE_A     - Sort by Title ascending
        TITLE_D     - Sort by Title descending
        OWNER_A     - Sort by Owner ascending
        OWNER_D     - Sort by Owner descending
     */
    $scope.docSortingState = 'ID_A';


    // Get setting regarding whether redlines are being used
    generalSettingsService.setting.get({setting: 'redline'}, function (data) {
        if (data.value === 'on' || data.value === 'optional') {
            $scope.isRedlineUsed = true;
        }
        else {
            $scope.isRedlineUsed = false;
        }
    }, function (error) {
        $scope.err = error;
    });


    // Get initial list of companies
    documentLookupService.first10.query(function(data) {
        $scope.documentSearchResults = data;
    }, function(error) {
        $scope.err = error;
    });

    $scope.$watch('searchString', function(newVal, oldVal) {
        $scope.executeSearch();
    });

    /* ---------- Controller Methods ----------- */

    $scope.executeSearch = function() {
        $timeout( function() {

            // Run if search strings are different
            if ($scope.searchString !== $scope.prevSearchString) {
                $scope.prevSearchString = $scope.searchString;

                documentLookupService.lookup.query({searchString: $scope.searchString}, function(data, status, headers, config) {
                    if (data.length > 0) {
                        $scope.noResultsFound = false;
                        $scope.documentSearchResults = data;
                    }
                    else {
                        $scope.documentSearchResults.length = 0;    // Clear previous search results
                        $scope.noResultsFound = true;
                    }

                }, function(data, status, headers, config) {
                    $scope.error = status;
                });
            }
        } , 500);
    };

    $scope.changeSortSearchState = function(searchType) {
        var currentState = $scope.docSortingState;
        var searchResults = $scope.documentSearchResults;

        switch (searchType) {
            case 'ID':
                if (currentState.substr(currentState.length-1) == 'A') {
                    searchResults.sort(function(a, b) {
                        if (a.id < b.id)
                            return 1;
                        if (a.id > b.id)
                            return -1;
                        return 0;
                    });
                    $scope.docSortingState = 'ID_D';
                }
                else {
                    searchResults.sort(function(a, b) {
                        if (a.id > b.id)
                            return 1;
                        if (a.id < b.id)
                            return -1;
                        return 0;
                    });
                    $scope.docSortingState = 'ID_A';
                }
                break;

            case 'TITLE':
                if (currentState.substr(currentState.length-1) == 'A') {
                    searchResults.sort(function(a, b) {
                        if (a.title < b.title)
                            return 1;
                        if (a.title > b.title)
                            return -1;
                        return 0;
                    });
                    $scope.docSortingState = 'TITLE_D';
                }
                else {
                    searchResults.sort(function(a, b) {
                        if (a.title > b.title)
                            return 1;
                        if (a.title < b.title)
                            return -1;
                        return 0;
                    });
                    $scope.docSortingState = 'TITLE_A';
                }
                break;

            case 'OWNER':
                if (currentState.substr(currentState.length-1) == 'A') {
                    searchResults.sort(function(a, b) {
                        if (a.organization.name < b.organization.name)
                            return 1;
                        if (a.organization.name > b.organization.name)
                            return -1;
                        return 0;
                    });
                    $scope.docSortingState = 'OWNER_D';
                }
                else {
                    searchResults.sort(function(a, b) {
                        if (a.organization.name > b.organization.name)
                            return 1;
                        if (a.organization.name < b.organization.name)
                            return -1;
                        return 0;
                    });
                    $scope.docSortingState = 'OWNER_A';
                }
                break;

            default:
                break;

            $scope.documentSearchResults = searchResults;
        }
    };

    $scope.changeActiveDocument = function(document) {
        $scope.getRevisions(document.id);
        $scope.getRecentDocumentActivity(document.id);
        $scope.activeDocument = document;
    };

    $scope.getRevisions = function(documentId) {
        if ($scope.lastFetchedRevisions != documentId) {
            documentLookupService.revision.query({documentId: documentId}, function(revisions) {
                $scope.revisions = revisions;
                $scope.lastFetchedRevisions = documentId;
            }, function(error) {
                $scope.err = error;
            });
        }
    };

    $scope.getRecentDocumentActivity = function(documentId) {
        if ($scope.lastFetchedActivity != documentId) {

            documentLookupService.recentApprovalHistory.query({documentId: documentId}, function(approvalHistory) {

                documentLookupService.documentComments.queryRecent({documentId: documentId}, function(recentComments) {
                    var recentDocActivity = approvalHistory.concat(recentComments);
                    $scope.lastFetchedActivity = documentId;

                    // Sort the recent activity array by date to interleave the approvalHistory with the recentComments
                    recentDocActivity.sort(function(a, b) {
                        if (a.date > b.date) {
                            return -1;
                        }
                        if (a.date < b.date) {
                            return 1;
                        }
                        return 0;
                    });
                    $scope.recentDocumentActivity = recentDocActivity;

                }, function(error) {
                    $scope.error = error;
                });

            }, function(error) {
                $scope.err = error;
            });
        }
    };

    $scope.addDocumentComment = function() {
        var newDocumentComment = {
            userId: $rootScope.user.userId,
            documentId: $scope.activeDocument.id,
            message: $scope.newDocumentComment
        };

        documentLookupService.documentComment.save(newDocumentComment, function(data, status, headers, config) {

            // Append newest comment to front of list
            $scope.recentDocumentActivity.unshift(data);

        }, function(data, status, headers, config) {
            $scope.error = status;
        });
    };

    $scope.open = function(steps) {
        var modalInstance = $modal.open({
            templateUrl: 'views/process-viewer/signoffPathModal.html',
            controller: signoffModalCtrl,
            resolve: {
                steps: function() {
                    return $scope.signoffPathSteps;
                }
            }
        });
    };

    $scope.openStepsModal = function(document) {

        // Prevent lookup of steps if already loaded previously
        if ($scope.prevDocIdStepsLookup == document.id) {
            $scope.open();
        }
        else {
            $scope.signoffPathSteps.length = 0;

            // Retrieve steps
            signoffPathsService.steps.query({documentId: document.id}, function (steps) {
                $scope.signoffPathSteps = steps;
                $scope.prevDocIdStepsLookup = document.id;
                $scope.open();
            }, function (error) {
                $scope.error = error;
            });
        }

    };

}

function documentCreationCtrl($scope, $rootScope, $window, $state, documentCreationService, signoffPathsService, generalSettingsService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Initialize file upload (dropzone)
    $scope.fileAdded = false;
    $scope.submitClicked = false;
    $scope.uploadSuccessful = false;
    $scope.creatingDocument = false;
    $scope.isRedline = false;   // Only true for revisions
    $scope.signoffRequired = false;

    // Keep track of form progress
    $scope.newDocumentForm = 1;

    // Initialize data for form
    $scope.documentType = {};
    $scope.documentTypes = [];
    $scope.organization = {};
    $scope.organizations = [];
    $scope.populatedDocumentTypesDropdown = false;
    $scope.populatedOrganizationsDropdown = false;
    $scope.signoffPathChoices = [];
    $scope.loadedPathsForOrg = 0;

    $scope.newDocument = {};
    $scope.signoffPath = {};
    $scope.signoffPaths = [];
    $scope.signoffPathSteps = [];

    $scope.fieldValidationFail = {};

    // Get signoff setting
    generalSettingsService.setting.get({setting: 'signoff'}, function (data) {
        if (data.value === 'on') {
            $scope.signoffRequired = true;
        }
        else {
            $scope.signoffRequired = false;
        }
    }, function (error) {
        $scope.err = error;
    });

    // Get initial data for form
    documentCreationService.documentType.query(function (documentTypes) {
        $scope.documentTypes = documentTypes;

        documentCreationService.organization.query(function (organizations) {
            $scope.organizations = organizations;
        }, function (error) {
            $scope.err = error;
        });

    }, function (error) {
        $scope.err = error;
    });


    // ------------------ Methods ------------------- //

    // Load sign off paths if organization choice changes
    $scope.loadSignoffPaths = function () {
        if ($scope.loadedPathsForOrg != $scope.newDocument.organization.organizationId) {
            signoffPathsService.pathMulti.query({orgId: $scope.newDocument.organization.organizationId}, function (data) {
                $scope.signoffPaths = data;
                $scope.loadedPathsForOrg = $scope.newDocument.organization.organizationId;
            }, function (error) {
                $scope.error = error;
            });
        }
    };

    $scope.$watch('signoffPath.selected.key.pathId', function (newVal, oldVal) {
        if (newVal != oldVal) {
            $scope.loadSignoffPathSteps(newVal);
        }
    });

    $scope.loadSignoffPathSteps = function (pathId) {
        signoffPathsService.templateSteps.query({pathId: pathId}, function (steps) {
            $scope.signoffPathSteps = steps;
        }, function (error) {
            $scope.error = error;
        });
    };

    $scope.goToStage = function (nextStage) {
        if (nextStage == 2) {
            if ($scope.validateForm()) {

                if ($scope.signoffRequired) {
                    $scope.loadSignoffPaths();
                    $scope.newDocumentForm = 2;
                }
                else {
                    $scope.newDocumentForm = 3;
                }
            }
        }
    };

    $scope.createNewDocument = function () {

        $scope.creatingDocument = true;

        // Append signoff path id
        if (typeof $scope.signoffPath.selected.key.pathId !== 'undefined') {
            $scope.newDocument.signoffPathId = $scope.signoffPath.selected.key.pathId;
        }
        else {
            $scope.newDocument.signoffPathId = null;
        }

        documentCreationService.document.save($scope.newDocument, function (data, status, headers, config) {
            $scope.documentId = data.id;
            $scope.uploadSuccessful = null; // Set to null because it will be set to true/false based on upload outcome
            $scope.tempUpload = false;
            $scope.processDropzone();

        }, function (data, status, headers, config) {
            $scope.err = status;
            $scope.creatingDocument = false;
        });
    };

    // Redirect after file uploaded via dropzone
    $scope.$watch('uploadSuccessful', function(newVal, oldVal) {
        if (newVal !== oldVal) {
            if (typeof oldVal !== 'undefined') {
                if (newVal === true) {
                    $scope.redirectToLookup();
                }
                if (newVal === false) {
                    $scope.creatingDocument = false;
                }
            }
        }
    });

    $scope.validateForm = function () {
        var validationFail = false;
        var title = $scope.newDocument.title;
        var documentType = $scope.newDocument.documentType;
        var organization = $scope.newDocument.organization;

        // Reset form validation error messages
        $scope.fieldValidationFail = {};

        if ((typeof title === 'undefined') || (title === '') || (title.length == 0)) {
            $scope.fieldValidationFail.title = true;
            validationFail = true;
        }
        if ((typeof documentType === 'undefined') || (documentType === '')) {
            $scope.fieldValidationFail.documentType = true;
            validationFail = true;
        }
        if ((typeof organization === 'undefined') || (organization === '')) {
            $scope.fieldValidationFail.organization = true;
            validationFail = true;
        }
        if (!$scope.fileAdded) {
            $scope.fieldValidationFail.file = true;
            validationFail = true;
        }

        return !validationFail;
    };

    $scope.redirectToLookup = function () {
        $state.go('process-viewer.document-lookup', {}, {reload: true});
    };

}

function documentRevisionCtrl($scope, $rootScope, $window, $state, $stateParams, documentCreationService, documentRevisionService, generalSettingsService, signoffPathsService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Keep track of form progress
    $scope.reviseDocumentForm = 1;
    $scope.documentId = $stateParams.documentId;

    // Default set redline flag to true
    $scope.redlineRequired = true;
    $scope.tempRevId = null;
    $scope.signoffRequired = false;
    $scope.docDownloadLink = '/resource/download?documentId=' + $scope.documentId;
    $scope.redlineDownloadLink = '/resource/download?documentId=' + $scope.documentId + '&isRedline=true';

    $scope.revision = {
        changeReason: ''
    };
    $scope.fieldValidationFail = {};

    // Keep track of file uploads
    $scope.fileAdded = false;
    $scope.uploadSuccessful = false;
    $scope.uploadingDocument = false;
    $scope.isRedline = false;
    $scope.uploadedDocument = undefined;
    $scope.uploadedRedline = undefined;

    $scope.signoffPaths = [];
    $scope.assocOrgs = [];

    // Get redline setting
    generalSettingsService.setting.get({setting: 'redline'}, function(data) {
        if (data.value === 'on') {
            $scope.redlineRequired = true;
        }
        else {
            $scope.redlineRequired = false;
        }
    }, function(error) {
        $scope.err = error;
    });

    // Get signoff setting
    generalSettingsService.setting.get({setting: 'signoff'}, function(data) {
        if (data.value === 'on') {
            $scope.signoffRequired = true;
        }
        else {
            $scope.signoffRequired = false;
        }
    }, function(error) {
        $scope.err = error;
    });

    // Get document and associated signoff path and steps
    documentCreationService.document.get({documentId: $scope.documentId}, function(document) {
        $scope.document = document;

        signoffPathsService.path.get({pathId: document.signoffPathId}, function(signoffPath) {
            $scope.signoffPath = signoffPath;

            signoffPathsService.templateSteps.query({pathId: signoffPath.key.pathId}, function(steps) {
                $scope.signoffPathSteps = steps;

                // Get list of organizations relating to step users
                var orgIds = [];
                for (var i = 0; i < steps.length; i++) {
                    orgIds.push(steps[i].user.primaryOrganization.organizationId);
                }

                documentCreationService.organizations.query({orgIds: orgIds}, function(assocOrgs) {
                    $scope.assocOrgs = assocOrgs;
                }, function(error) {
                    $scope.error = error;
                })

            }, function(error) {
                $scope.error = error;
            })

        }, function(error) {
            $scope.error = error;
        });

    }, function(error) {
        $scope.error = error;
    });


    // ------------------ Methods ------------------- //

    $scope.uploadDocument = function(isRedline) {

        if ($scope.fileAdded) {

            // Only upload if it is a new file
            if (((isRedline) && ($scope.uploadedDocument != $scope.file.name)) ||
                ((!isRedline) && ($scope.uploadedRedline != $scope.file.name))) {
                isRedline ? $scope.isRedline = true : $scope.isRedline = false;

                $scope.tempUpload = true;
                $scope.uploadingDocument = true;
                $scope.processDropzone();

                isRedline ? $scope.uploadedRedline = $scope.file.name : $scope.uploadedDocument = $scope.file.name;
                isRedline ? $scope.fieldValidationFail.uploadedRedline = false : $scope.fieldValidationFail.uploadedDocument = false;
            }
        }
    };

    // Watch directive for when upload completes
    $scope.$watch('uploadSuccessful', function(newVal, oldVal) {
        if (newVal !== oldVal) {
            if (newVal == true) {
                $scope.uploadSuccessful = false;        // Reset flag
                $scope.uploadingDocument = false;
                $scope.fileAdded = false;
                $scope.resetDropzone();
            }
        }
    });

    // Advance form to next step
    $scope.goToNextStage = function(nextStage) {
        if (nextStage == 2) {
            if ($scope.validateForm()) {

                // Check for required sign-off
                if ($scope.signoffRequired) {
                    $scope.reviseDocumentForm = 2;

                    // Update document download links
                    $scope.docDownloadLink += '&revisionId=' + $scope.tempRevId;
                    $scope.redlineDownloadLink += '&revisionId=' + $scope.tempRevId;
                }
                else {
                    $scope.reviseDocumentForm = 3;
                }
            }
        }
    };

    $scope.validateForm = function() {
        var validationFail = false;
        var changeReason = $scope.revision.changeReason;
        var doc = $scope.uploadedDocument;
        var redline = $scope.uploadedRedline;

        // Reset form validation error messages
        $scope.fieldValidationFail = {};

        if ( (typeof changeReason === 'undefined') || (changeReason === '') || (changeReason.length == 0) ) {
            $scope.fieldValidationFail.changeReason = true;
            validationFail = true;
        }
        if (typeof doc === 'undefined') {
            $scope.fieldValidationFail.uploadedDocument = true;
            validationFail = true;
        }
        if ($scope.redlineRequired) {
            if (typeof redline === 'undefined') {
                $scope.fieldValidationFail.uploadedRedline = true;
                validationFail = true;
            }
        }

        return !validationFail;
    };

    $scope.addRevision = function() {
        var revisionPayload = {
            documentId: $scope.documentId,
            changeReason: $scope.revision.changeReason,
            changeUserEmail: $rootScope.user.userName
        };

        if ($scope.redlineRequired) {
            revisionPayload.redlineDocPresent = true;
        }
        else {
            revisionPayload.redlineDocPresent = false;
        }

        documentRevisionService.revision.save(revisionPayload, function(data, status, headers, config) {
            $scope.revision = data.key.revisionId;

            documentRevisionService.updateUploadRevisionId.update({documentId: $scope.documentId, tempRevId: $scope.tempRevId, newRevId: data.key.revisionId},
                function(data) {
                    $state.go('process-viewer.document-lookup', {}, {reload: true});
                }, function(error) {
                });

        }, function(data, status, headers, config) {
            $scope.err = status;
        });
    };

    $scope.cancelRevision = function() {

        // Delete temporary documents upon cancel
        if ($scope.tempRevId != null) {
            documentRevisionService.upload.remove({documentId: $scope.documentId, tempRevId: $scope.tempRevId}, function(data) {
                $state.go('process-viewer.document-lookup');
            }, function(error) {
                $scope.error = error;
                $state.go('process-viewer.document-lookup', {}, {reload: true});
            });
        }
        else {
            $state.go('process-viewer.document-lookup', {}, {reload: true});
        }
    };

    /* ----------- Helpers ------------ */

    $scope.getOrgNameById = function(organizationId) {

        var orgs = $scope.assocOrgs;
        for (var i = 0; i < orgs.length; i++) {
            if (orgs[i].organizationId == organizationId) {
                return orgs[i].name;
            }
        }
        return '';
    };

}

function signoffModalCtrl($scope, $modalInstance, steps) {
    $scope.steps = steps;

    $scope.ok = function() {
        $modalInstance.close();
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    }
}

angular
    .module('provesoft')
    .controller('documentLookupCtrl', documentLookupCtrl)
    .controller('documentCreationCtrl', documentCreationCtrl)
    .controller('documentRevisionCtrl', documentRevisionCtrl)
    .controller('signoffModalCtrl', signoffModalCtrl);
