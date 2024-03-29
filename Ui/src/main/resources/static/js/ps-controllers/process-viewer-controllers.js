'use strict';

function documentLookupCtrl($scope, $rootScope, $window, $q, $timeout, $modal, userService, generalSettingsService, documentLookupService, signoffPathsService, likeService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // Holds all initial queries to load activity
    $scope.initActivityResults = [];
    $scope.error = '';

    $scope.isRedlineUsed = false;

    $scope.noResultsFound = false;
    $scope.searchString = '';
    $scope.prevSearchString = '';
    $scope.includeObsolete = false;
    $scope.documentSearchResults = [];
    $scope.revisions = [];
    $scope.recentDocumentActivity = [];

    // ng-model for new comment
    $scope.newDocumentComment = '';
    $scope.newChildComment = '';

    $scope.activeDocument = null;               // Keep track of current active document (right panel timeline)
    $scope.lastFetchedRevisions = '';           // Keep track of which document revisions were last fetched for (documentId)
    $scope.lastFetchedActivity = '';            // Keep track of which document activities were last fetched (documentId)

    // Steps associated with signoff path, used in modal
    $scope.signoffPathSteps = [];

    $scope.revApprovalHistory = [];             // Hold last retrieved approval history for revision

    $scope.prevDocIdStepsLookup = -1;
    $scope.prevRevIdLookup = -1;

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
    documentLookupService.document.queryFirst10(function(data) {
        $scope.documentSearchResults = data;
    }, function(error) {
        $scope.err = error;
    });

    $scope.$watch('searchString', function(newVal, oldVal) {
        // Run if search strings are different and are at least length 1
        if ( ($scope.searchString !== $scope.prevSearchString) && ($scope.searchString.length > 0) ) {
            $scope.prevSearchString = $scope.searchString;
            $scope.executeSearch();
        }
    });

    $scope.$watch('includeObsolete', function(newVal, oldVal) {
        if (($scope.searchString.length > 0)) {
            $scope.executeSearch();
        }
    });

    /* ---------- Controller Methods ----------- */

    $scope.executeSearch = function() {
        $timeout( function() {
            documentLookupService.document.queryBySearchString({searchString: $scope.searchString, includeObsolete: $scope.includeObsolete}, function(data, status, headers, config) {
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
            documentLookupService.revision.queryByDocumentId({documentId: documentId}, function(revisions) {
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

                documentLookupService.documentComment.queryRecentByDocumentId({documentId: documentId}, function(recentComments) {
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

                    // Format date
                    $scope.recentDocumentActivity = $scope.formatDateForList(recentDocActivity);

                    // Get list of userIds for profile picture lookup
                    var userIds = [];
                    for (var z = 0; z < recentDocActivity.length; z++) {
                        userIds.push(recentDocActivity[z].user.userId);
                    }

                    // Get list of commentIds for comment like lookup
                    var documentCommentIds = [];
                    for (var y = 0; y < recentComments.length; y++) {
                        documentCommentIds.push(recentComments[y].id);
                    }

                    // Append comment likes
                    // Append child comments
                    if (documentCommentIds.length > 0) {
                        documentLookupService.childDocumentComment.query({parentCommentIds: documentCommentIds}, function(childComments) {
                            // Append child comment profile pictures
                            var childCommentUserIds = [];
                            for (var w = 0; w < childComments.length; w++) {
                                childCommentUserIds.push(childComments[w].user.userId);
                            }

                            userService.profilePicture.query({userIds: childCommentUserIds}, function(childProfilePics) {
                                childComments = $scope.matchProfilePicToList(childProfilePics, childComments);
                                $scope.matchChildCommentsToParent(childComments);
                            }, function(error) {
                                $scope.error = error;
                                $scope.matchChildCommentsToParent(childComments);
                            });

                        }, function(error) {
                            $scope.error = error;
                        });

                        likeService.documentCommentLike.query({documentCommentIds: documentCommentIds}, function(likes) {
                            $scope.matchLikesToComment(likes);
                        }, function(error) {
                            $scope.error = error;
                        });
                    }

                    // Append parent comment profile pictures
                    if (userIds.length > 0) {
                        userService.profilePicture.query({userIds: userIds}, function(profilePictures) {
                            $scope.matchProfilePicToActivity(profilePictures);

                        }, function(error) {
                            $scope.error = error;
                        });
                    }

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
            documentId: $scope.activeDocument.id,
            message: $scope.newDocumentComment
        };

        documentLookupService.documentComment.save(newDocumentComment, function(data, status, headers, config) {

            // Append my profile picture
            data.profilePicture = $scope.$parent.profilePicture;

            // Append newest comment to front of list
            $scope.recentDocumentActivity.unshift(data);

            // Reset ng-model
            $scope.newDocumentComment = '';

        }, function(data, status, headers, config) {
            $scope.error = status;
        });
    };

    $scope.addChildComment = function(parentCommentId) {
        var newChildComment = {
            documentId: $scope.activeDocument.id,
            parentCommentId: parentCommentId,
            message: $scope.newChildComment
        };

        documentLookupService.documentComment.save(newChildComment, function(data, status, headers, config) {

            // Append my profile picture
            data.profilePicture = $scope.$parent.profilePicture;
            data.date = $scope.formatDate(data.date);

            // Append newest comment to chain
            var recentDocActivity = $scope.recentDocumentActivity;
            for (var i = 0; i < recentDocActivity.length; i++) {
                if (recentDocActivity[i].hasOwnProperty('message')) {
                    if (recentDocActivity[i].id === parentCommentId) {
                        // Initialize childComments if none exist
                        if (typeof recentDocActivity[i].childComments === 'undefined') {
                            $scope.recentDocumentActivity[i].childComments = [];
                        }
                        $scope.recentDocumentActivity[i].childComments.push(data);
                    }
                }
            }

            // Reset ng-model
            $scope.newChildComment = '';

        }, function(data, status, headers, config) {
            $scope.error = status;
        });
    };

    $scope.addLikeForComment = function(commentId) {

        likeService.documentCommentLike.save({documentCommentId: commentId}, function(data, status, headers, config) {
            // Mark comment as me liking it
            var recentDocActivity = $scope.recentDocumentActivity;
            for (var i = 0; i < recentDocActivity.length; i++) {
                if (recentDocActivity[i].hasOwnProperty('message')) {
                    if (recentDocActivity[i].id === commentId) {
                        $scope.recentDocumentActivity[i].iLikedIt = true;
                        $scope.recentDocumentActivity[i].numberOfLikes++;
                    }
                }
            }

        }, function(data, status, headers, config) {
            $scope.error = status;
        })
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
        if ($scope.prevDocIdStepsLookup == document.id && $scope.prevRevIdLookup == document.revision) {
            $scope.open();
        }
        else {
            $scope.signoffPathSteps.length = 0;

            // Retrieve steps
            signoffPathsService.steps.query({documentId: document.id}, function (steps) {
                $scope.signoffPathSteps = steps;
                $scope.prevDocIdStepsLookup = document.id;
                $scope.prevRevIdLookup = document.revision;
                $scope.open();
            }, function (error) {
                $scope.error = error;
            });
        }

    };

    $scope.openApprovalHistory = function() {
        var modalInstance = $modal.open({
            templateUrl:'views/process-viewer/approvalHistoryModal.html',
            controller: approvalHistoryModalCtrl,
            resolve: {
                history: function() {
                    return $scope.revApprovalHistory;
                }
            }
        })
    };

    $scope.openApprovalHistoryModal = function(documentId, revisionId) {

        // Prevent lookup of steps if already loaded previously
        if ($scope.prevDocIdStepsLookup == documentId && $scope.prevRevIdLookup == revisionId) {
            $scope.openApprovalHistory();
        }
        else {
            // Retrieve Approvals
            documentLookupService.approvalHistory.queryByDocumentAndRevision({documentId: documentId, revisionId: revisionId}, function (history) {
                $scope.revApprovalHistory = history;
                $scope.prevDocIdStepsLookup = documentId;
                $scope.prevRevIdLookup = revisionId;
                $scope.openApprovalHistory();
            }, function (error) {
                $scope.error = error;
            });
        }
    };

    $scope.formatDateForList = function(docActivity) {
        var currentDate = new Date();
        for (var i = 0; i < docActivity.length; i++) {

            var docDate = new Date(docActivity[i].date);

            if ( docDate.getFullYear()==currentDate.getFullYear() &&
                docDate.getMonth()==currentDate.getMonth() &&
                docDate.getDate()==currentDate.getDate()
            ) {
                docActivity[i].date = 'Today at '+ docDate.getHours() + ':' + (docDate.getMinutes()<10?'0':'') + docDate.getMinutes();
            }
        }

        return docActivity;
    };

    $scope.formatDate = function(rawDate) {
        var currentDate = new Date();
        var date = new Date(rawDate);
        if ( date.getFullYear()==currentDate.getFullYear() &&
            date.getMonth()==currentDate.getMonth() &&
            date.getDate()==currentDate.getDate()
        ) {
            return 'Today at '+ date.getHours() + ':' + (date.getMinutes()<10?'0':'') + date.getMinutes();
        }
        return date;
    };

    // Helper function to match child comments to parent comment
    $scope.matchChildCommentsToParent = function(childComments) {
        var recentDocActivity = $scope.recentDocumentActivity;
        for (var q = 0; q < recentDocActivity.length; q++) {

            // Only done for comments
            if (recentDocActivity[q].hasOwnProperty('message')) {
                recentDocActivity[q].childComments = [];
                for (var r = 0; r < childComments.length; r++) {
                    if (recentDocActivity[q].id === childComments[r].parentCommentId) {
                        childComments[r].date = $scope.formatDate(childComments[r].date);
                        recentDocActivity[q].childComments.push(childComments[r]);
                    }
                }
            }
        }
    };

    // Helper function to line up likes with document comment
    $scope.matchLikesToComment = function(likes) {
        var myUserId = $rootScope.userDetails.userId;

        var recentDocActivity = $scope.recentDocumentActivity;
        for (var f = 0; f < recentDocActivity.length; f++) {

            // Only add likes for comments
            if (recentDocActivity[f].hasOwnProperty('message')) {
                $scope.recentDocumentActivity[f].numberOfLikes = 0;

                for (var g = 0; g < likes.length; g++) {
                    if (recentDocActivity[f].id === likes[g].key.documentCommentId) {
                        $scope.recentDocumentActivity[f].numberOfLikes++;

                        // Check if I liked it
                        if (likes[g].key.userId === myUserId) {
                            $scope.recentDocumentActivity[f].iLikedIt = true;
                        }
                    }
                }
            }
        }
    };

    // Helper function to line up profile picture with activity
    $scope.matchProfilePicToActivity = function(profilePictures) {
        var recentDocActivity = $scope.recentDocumentActivity;
        for (var o = 0; o < recentDocActivity.length; o++) {
            for (var p = 0; p < profilePictures.length; p++) {
                if (recentDocActivity[o].user.userId === profilePictures[p].userId) {
                    $scope.recentDocumentActivity[o].profilePicture = profilePictures[p].picData;
                }
            }
        }
    };

    // Helper function to line up profile picture with an arbitrary list
    $scope.matchProfilePicToList = function(profilePictures, list) {
        for (var e = 0; e < list.length; e++) {
            for (var f = 0; f < profilePictures.length; f++) {
                if (list[e].user.userId === profilePictures[f].userId) {
                    list[e].profilePicture = profilePictures[f].picData;
                }
            }
        }
        return list;
    };

}

function documentCreationCtrl($scope, $rootScope, $window, $state, documentCreationService, signoffPathsService, generalSettingsService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Initialize file upload (dropzone)
    $scope.dropzoneConfig = {
        url: '/resource/upload',
        maxFileSize: 100,
        paramName: "uploadfile",
        autoProcessQueue: false
    };
    $scope.isDocumentUpload = true;

    $scope.fileAdded = false;
    $scope.submitClicked = false;
    $scope.uploadSuccessful = false;
    $scope.creatingDocument = false;
    $scope.progress = 0;
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
            signoffPathsService.path.query({orgId: $scope.newDocument.organization.organizationId}, function (data) {
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
            $scope.validateStage1Form(function(result) {
                if (result) {
                    if ($scope.signoffRequired) {
                        $scope.loadSignoffPaths();
                        $scope.newDocumentForm = 2;
                    }
                    else {
                        $scope.newDocumentForm = 3;
                    }
                }
            });
        }
        else {
            if ($scope.signoffRequired) {
                if ($scope.validateStage2Form()) {
                    $scope.newDocumentForm = 3;
                }
            }
            else {
                $scope.newDocumentForm = 3;
            }
        }
    };

    $scope.createNewDocument = function () {

        $scope.creatingDocument = true;

        // Append signoff path id
        if ( (typeof $scope.signoffPath.selected !== 'undefined') &&
             (typeof $scope.signoffPath.selected.key.pathId !== 'undefined') )
        {
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

    $scope.validateStage1Form = function (callback) {
        var validationFail = false;
        var title = $scope.newDocument.title;
        var documentType = $scope.newDocument.documentType;
        var organization = $scope.newDocument.organization;

        // Reset form validation error messages
        $scope.fieldValidationFail = {};

        if ((typeof title === 'undefined') || (title === '') || (title.length == 0)) {
            $scope.fieldValidationFail.title = 'Please enter a title';
            validationFail = true;
        }
        else if (title.length > 250) {
            $scope.fieldValidationFail.title = 'Please limit title to 250 characters';
            validationFail = true;
        }

        if ((typeof documentType === 'undefined') || (documentType === '')) {
            $scope.fieldValidationFail.documentType = 'Please select a document type';
            validationFail = true;
        }
        if ((typeof organization === 'undefined') || (organization === '')) {
            $scope.fieldValidationFail.organization = 'Please select an organization';
            validationFail = true;
        }

        if (!$scope.fileAdded) {
            $scope.fieldValidationFail.file = 'Please upload a file';
            validationFail = true;
        }

        if (!validationFail) {
            documentCreationService.check.byTitle({title: title}, function(data) {
                if (!data.exists) {
                    return callback(true);
                }
                else {
                    $scope.fieldValidationFail.title = 'Document with this title already exists';
                    return callback(false);
                }
            }, function(error) {
                $scope.fieldValidationFail.title = 'Error validating new document';
                return callback(false);
            });
        }
    };

    $scope.validateStage2Form = function() {
        var path = $scope.signoffPath.selected;
        if ((typeof path === 'undefined') || (path === '') || (path === null)) {
            $scope.fieldValidationFail.signoffPath = 'Please select a signoff path';
            return false;
        }
        else {
            return true;
        }
    };

    $scope.redirectToLookup = function () {
        $state.go('process-viewer.document-lookup', {}, {reload: true});
    };

}

function documentRevisionCtrl($scope, $rootScope, $window, $state, $stateParams, documentLookupService, documentCreationService, documentRevisionService, generalSettingsService, signoffPathsService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Dropzone element
    $scope.dropzoneConfig = {
        url: '/resource/upload',
        maxFileSize: 100,
        paramName: "uploadfile",
        autoProcessQueue: false
    };
    $scope.isDocumentUpload = true;

    // Keep track of form progress
    $scope.reviseDocumentForm = 1;
    $scope.documentId = $stateParams.documentId;
    $scope.revisionInProgress = false;

    // Default set redline flag to true
    $scope.redlineRequired = true;
    $scope.tempRevId = null;
    $scope.signoffRequired = false;
    $scope.docDownloadLink = '/resource/download?documentId=' + $scope.documentId;
    $scope.redlineDownloadLink = '/resource/download?documentId=' + $scope.documentId + '&isRedline=true';

    $scope.revision = {
        changeReason: '',
        makeObsolete: false,
        newSignoffPath: null
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

        // Get document and associated signoff path and steps
        documentLookupService.document.getByDocumentId({documentId: $scope.documentId}, function(document) {
            $scope.document = document;

            if (document.signoffPathId !== null) {

                signoffPathsService.path.get({pathId: document.signoffPathId}, function(signoffPath) {
                    $scope.signoffPath = signoffPath;

                    signoffPathsService.templateSteps.query({pathId: signoffPath.key.pathId}, function(steps) {
                        $scope.signoffPathSteps = steps;

                        // Get list of organizations relating to step users
                        var orgIds = [];
                        for (var i = 0; i < steps.length; i++) {
                            orgIds.push(steps[i].user.primaryOrganization.organizationId);
                        }

                        documentCreationService.organization.queryByOrgIds({orgIds: orgIds}, function(assocOrgs) {
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
            }
            else {
                // Retrieve list of signoff paths if path is null and paths are required
                signoffPathsService.path.query({orgId: document.organization.organizationId}, function(signoffPaths) {
                    $scope.signoffPaths = signoffPaths;
                }, function(error) {
                    $scope.error = error;
                });
            }

        }, function(error) {
            $scope.error = error;
        });

    }, function(error) {
        $scope.err = error;
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

        else if (nextStage == 3) {
            if ( $scope.signoffRequired && ($scope.document.signoffPathId === null) && ($scope.revision.newSignoffPath === null) ) {
                $scope.fieldValidationFail.signoffPath = 'Please select a Signoff Path';
            }
            else {
                $scope.reviseDocumentForm = 3;
                delete $scope.fieldValidationFail.signoffPath;
            }
        }
    };

    // Watch for change to signoff path selection to update steps
    // (Only done when signoff path was originally null for the document)
    $scope.$watch('revision.newSignoffPath.key.pathId', function (newVal, oldVal) {
        if (newVal !== oldVal) {
            if (newVal == true) {
                $scope.loadSignoffPathSteps(newVal);
            }
        }
    });

    $scope.loadSignoffPathSteps = function (pathId) {
        signoffPathsService.templateSteps.query({pathId: pathId}, function (steps) {
            $scope.signoffPathSteps = steps;
        }, function (error) {
            $scope.error = error;
        });
    };

    $scope.validateForm = function() {
        var validationFail = false;
        var changeReason = $scope.revision.changeReason;
        var doc = $scope.uploadedDocument;
        var redline = $scope.uploadedRedline;

        // Reset form validation error messages
        $scope.fieldValidationFail = {};

        if ( (typeof changeReason === 'undefined') || (changeReason === '') || (changeReason.length == 0) ) {
            $scope.fieldValidationFail.changeReason = 'Please enter a change reason';
            validationFail = true;
        }
        else if (changeReason.length > 1000) {
            $scope.fieldValidationFail.changeReason = 'Please limit reason to 1000 characters';
            validationFail = true;
        }

        if (!$scope.revision.makeObsolete) {
            if (typeof doc === 'undefined') {
                $scope.fieldValidationFail.uploadedDocument = 'Please upload a revised document';
                validationFail = true;
            }
            if ($scope.redlineRequired) {
                if (typeof redline === 'undefined') {
                    $scope.fieldValidationFail.uploadedRedline = 'Please upload the corresponding redlined document';
                    validationFail = true;
                }
            }
        }

        return !validationFail;
    };

    $scope.addRevision = function() {
        $scope.revisionInProgress = true;

        var revisionPayload = {
            documentId: $scope.documentId,
            changeReason: $scope.revision.changeReason,
            changeUserEmail: $rootScope.user.userName,
            changeToObsolete: $scope.revision.makeObsolete
        };

        if ($scope.redlineRequired) {
            revisionPayload.redlineDocPresent = true;
        }
        else {
            revisionPayload.redlineDocPresent = false;
        }

        // Update signoff path
        if ( ($scope.document.signoffPathId === null) && ($scope.revision.newSignoffPath !== null) ) {
            documentCreationService.document.addSignoffPath({documentId: $scope.documentId, signoffPathId: $scope.revision.newSignoffPath.key.pathId}, function(data) {

                // Update revision
                documentRevisionService.revision.save(revisionPayload, function(data, status, headers, config) {
                    if (!$scope.revision.makeObsolete) {
                        documentRevisionService.updateUploadRevisionId.update({documentId: $scope.documentId, tempRevId: $scope.tempRevId, newRevId: data.key.revisionId},
                            function(data) {
                                $state.go('process-viewer.document-lookup', {}, {reload: true});
                            }, function(error) {
                                $scope.error = error;
                            });
                    }
                    else {
                        $state.go('process-viewer.document-lookup', {}, {reload: true});
                    }
                }, function(data, status, headers, config) {
                    $scope.revisionInProgress = false;
                    $scope.err = status;
                });

            }, function(error) {
                $scope.error = error;
                $scope.revisionInProgress = false;
            });
        }
        else {
            // Update revision
            documentRevisionService.revision.save(revisionPayload, function(data, status, headers, config) {
                if (!$scope.revision.makeObsolete) {
                    documentRevisionService.updateUploadRevisionId.update({documentId: $scope.documentId, tempRevId: $scope.tempRevId, newRevId: data.key.revisionId},
                        function(data) {
                            $state.go('process-viewer.document-lookup', {}, {reload: true});
                        }, function(error) {
                            $scope.error = error;
                            $scope.revisionInProgress = false;
                        });
                }
                else {
                    $state.go('process-viewer.document-lookup', {}, {reload: true});
                }
            }, function(data, status, headers, config) {
                $scope.err = status;
                $scope.revisionInProgress = false;
            });
        }

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

function treeViewCtrl($scope, documentCreationService, documentLookupService, userService, likeService) {

    $scope.organizations = [];
    $scope.documentTypes = [];
    $scope.activeDocument = {};
    $scope.recentDocumentActivity = [];

    $scope.previousOrganizationId = '';
    $scope.previousDocumentTypeId = '';

    // Initialize
    documentCreationService.organization.query(function(organizations) {
        $scope.organizations = organizations;
    }, function(error) {
        $scope.error = error;
    });

    $scope.getDocuments = function(organization) {
        if (organization.organizationId !== $scope.previousOrganizationId) {
            $scope.documentTypes.length = 0;
            documentLookupService.document.queryByOrganizationId({organizationId: organization.organizationId}, function(documents) {
                if (documents.length > 0) {
                    groupDocumentsByDocumentType(documents);
                }
                $scope.previousOrganizationId = organization.organizationId;
            }, function(error) {
                $scope.error = error;
            })
        }
    };

    $scope.getRecentDocumentActivity = function(document) {
        var documentId = document.id;
        $scope.activeDocument = document;

        if ($scope.lastFetchedActivity != documentId) {

            documentLookupService.recentApprovalHistory.query({documentId: documentId}, function(approvalHistory) {

                documentLookupService.documentComment.queryRecentByDocumentId({documentId: documentId}, function(recentComments) {
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

                    // Format date
                    $scope.recentDocumentActivity = $scope.formatDateForList(recentDocActivity);

                    // Get list of userIds for profile picture lookup
                    var userIds = [];
                    for (var z = 0; z < recentDocActivity.length; z++) {
                        userIds.push(recentDocActivity[z].user.userId);
                    }

                    // Get list of commentIds for comment like lookup
                    var documentCommentIds = [];
                    for (var y = 0; y < recentComments.length; y++) {
                        documentCommentIds.push(recentComments[y].id);
                    }

                    // Append comment likes
                    // Append child comments
                    if (documentCommentIds.length > 0) {
                        documentLookupService.childDocumentComment.query({parentCommentIds: documentCommentIds}, function(childComments) {
                            // Append child comment profile pictures
                            var childCommentUserIds = [];
                            for (var w = 0; w < childComments.length; w++) {
                                childCommentUserIds.push(childComments[w].user.userId);
                            }

                            userService.profilePicture.query({userIds: childCommentUserIds}, function(childProfilePics) {
                                childComments = $scope.matchProfilePicToList(childProfilePics, childComments);
                                $scope.matchChildCommentsToParent(childComments);
                            }, function(error) {
                                $scope.error = error;
                                $scope.matchChildCommentsToParent(childComments);
                            });

                        }, function(error) {
                            $scope.error = error;
                        });

                        likeService.documentCommentLike.query({documentCommentIds: documentCommentIds}, function(likes) {
                            $scope.matchLikesToComment(likes);
                        }, function(error) {
                            $scope.error = error;
                        });
                    }

                    // Append parent comment profile pictures
                    if (userIds.length > 0) {
                        userService.profilePicture.query({userIds: userIds}, function(profilePictures) {
                            $scope.matchProfilePicToActivity(profilePictures);

                        }, function(error) {
                            $scope.error = error;
                        });
                    }

                }, function(error) {
                    $scope.error = error;
                });

            }, function(error) {
                $scope.err = error;
            });
        }
    };

    $scope.formatDateForList = function(docActivity) {
        var currentDate = new Date();
        for (var i = 0; i < docActivity.length; i++) {

            var docDate = new Date(docActivity[i].date);

            if ( docDate.getFullYear()==currentDate.getFullYear() &&
                docDate.getMonth()==currentDate.getMonth() &&
                docDate.getDate()==currentDate.getDate()
            ) {
                docActivity[i].date = 'Today at '+ docDate.getHours() + ':' + (docDate.getMinutes()<10?'0':'') + docDate.getMinutes();
            }
        }

        return docActivity;
    };

    $scope.formatDate = function(rawDate) {
        var currentDate = new Date();
        var date = new Date(rawDate);
        if ( date.getFullYear()==currentDate.getFullYear() &&
            date.getMonth()==currentDate.getMonth() &&
            date.getDate()==currentDate.getDate()
        ) {
            return 'Today at '+ date.getHours() + ':' + (date.getMinutes()<10?'0':'') + date.getMinutes();
        }
        return date;
    };

    // Helper function to match child comments to parent comment
    $scope.matchChildCommentsToParent = function(childComments) {
        var recentDocActivity = $scope.recentDocumentActivity;
        for (var q = 0; q < recentDocActivity.length; q++) {

            // Only done for comments
            if (recentDocActivity[q].hasOwnProperty('message')) {
                recentDocActivity[q].childComments = [];
                for (var r = 0; r < childComments.length; r++) {
                    if (recentDocActivity[q].id === childComments[r].parentCommentId) {
                        childComments[r].date = $scope.formatDate(childComments[r].date);
                        recentDocActivity[q].childComments.push(childComments[r]);
                    }
                }
            }
        }
    };

    // Helper function to line up likes with document comment
    $scope.matchLikesToComment = function(likes) {
        var myUserId = $rootScope.userDetails.userId;

        var recentDocActivity = $scope.recentDocumentActivity;
        for (var f = 0; f < recentDocActivity.length; f++) {

            // Only add likes for comments
            if (recentDocActivity[f].hasOwnProperty('message')) {
                $scope.recentDocumentActivity[f].numberOfLikes = 0;

                for (var g = 0; g < likes.length; g++) {
                    if (recentDocActivity[f].id === likes[g].key.documentCommentId) {
                        $scope.recentDocumentActivity[f].numberOfLikes++;

                        // Check if I liked it
                        if (likes[g].key.userId === myUserId) {
                            $scope.recentDocumentActivity[f].iLikedIt = true;
                        }
                    }
                }
            }
        }
    };

    // Helper function to line up profile picture with activity
    $scope.matchProfilePicToActivity = function(profilePictures) {
        var recentDocActivity = $scope.recentDocumentActivity;
        for (var o = 0; o < recentDocActivity.length; o++) {
            for (var p = 0; p < profilePictures.length; p++) {
                if (recentDocActivity[o].user.userId === profilePictures[p].userId) {
                    $scope.recentDocumentActivity[o].profilePicture = profilePictures[p].picData;
                }
            }
        }
    };

    // Helper function to line up profile picture with an arbitrary list
    $scope.matchProfilePicToList = function(profilePictures, list) {
        for (var e = 0; e < list.length; e++) {
            for (var f = 0; f < profilePictures.length; f++) {
                if (list[e].user.userId === profilePictures[f].userId) {
                    list[e].profilePicture = profilePictures[f].picData;
                }
            }
        }
        return list;
    };


    function groupDocumentsByDocumentType(documents) {
        var prevDocTypeId = documents[0].documentType.id;
        var documentTypes = [];

        documentTypes.push(documents[0].documentType);
        documentTypes[0].setOfDocs = [];
        documentTypes[0].setOfDocs.push(documents[0]);

        // Skip obsolete
        for (var i = 1, j = 0; i < documents.length; i++) {
            if (documents[i].state === 'Obsolete') {
                continue;
            }

            if (documents[i].documentType.id === prevDocTypeId) {
                documentTypes[j].setOfDocs.push(documents[i]);
            }
            else {
                documentTypes.push(documents[i].documentType);
                j++;
                documentTypes[j].setOfDocs = [];
                documentTypes[j].setOfDocs.push(documents[i]);
                prevDocTypeId = documents[i].documentType.id;
            }
        }

        $scope.documentTypes = documentTypes;
    }

}

function signoffModalCtrl($scope, $modalInstance, steps, userService) {

    $scope.ok = function() {
        $modalInstance.close();
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

    function formatDateForList(list) {
        var currentDate = new Date();
        for (var i = 0; i < list.length; i++) {

            var docDate = new Date(list[i].approvalDate);

            if ( docDate.getFullYear()==currentDate.getFullYear() &&
                docDate.getMonth()==currentDate.getMonth() &&
                docDate.getDate()==currentDate.getDate()
            ) {
                list[i].approvalDate = 'Today at '+ docDate.getHours() + ':' + (docDate.getMinutes()<10?'0':'') + docDate.getMinutes();
            }
        }
        return list;
    }

    function matchProfilePicToStep(profilePics) {
        var steps = $scope.steps;
        for (var i = 0; i < steps.length; i++) {
            for (var j = 0; j < profilePics.length; j++) {
                if (steps[i].user.userId === profilePics[j].userId) {
                    steps[i].profilePicture = profilePics[j].picData;
                }
            }
        }
        $scope.steps = steps;
    }


    // Initialize modal

    $scope.steps = formatDateForList(steps);

    var userIds = [];
    for (var j = 0; j < steps.length; j++) {
        userIds.push(steps[j].user.userId);
    }
    userService.profilePicture.query({userIds: userIds}, function(profilePics) {
        matchProfilePicToStep(profilePics);
    }, function(error) {
        $scope.error = error;
    });
}

function approvalHistoryModalCtrl($scope, $modalInstance, history, userService) {

    $scope.ok = function() {
        $modalInstance.close();
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

    function formatDateForList(list) {
        var currentDate = new Date();
        for (var i = 0; i < list.length; i++) {

            var docDate = new Date(list[i].date);

            if ( docDate.getFullYear()==currentDate.getFullYear() &&
                docDate.getMonth()==currentDate.getMonth() &&
                docDate.getDate()==currentDate.getDate()
            ) {
                list[i].date = 'Today at '+ docDate.getHours() + ':' + (docDate.getMinutes()<10?'0':'') + docDate.getMinutes();
            }
        }
        return list;
    }

    function matchProfilePicToStep(profilePics) {
        var history = $scope.history;
        for (var i = 0; i < history.length; i++) {
            for (var j = 0; j < profilePics.length; j++) {
                if (history[i].user.userId === profilePics[j].userId) {
                    history[i].profilePicture = profilePics[j].picData;
                }
            }
        }
        $scope.history = history;
    }


    // Initialize modal

    $scope.history = formatDateForList(history);

    var userIds = [];
    for (var j = 0; j < history.length; j++) {
        userIds.push(history[j].user.userId);
    }
    userService.profilePicture.query({userIds: userIds}, function(profilePics) {
        matchProfilePicToStep(profilePics);
    }, function(error) {
        $scope.error = error;
    });
}

angular
    .module('provesoft')
    .controller('documentLookupCtrl', documentLookupCtrl)
    .controller('documentCreationCtrl', documentCreationCtrl)
    .controller('documentRevisionCtrl', documentRevisionCtrl)
    .controller('treeViewCtrl', treeViewCtrl)
    .controller('signoffModalCtrl', signoffModalCtrl);
