'use strict';

function MainCtrl($scope, $rootScope, $window, authService) {

     // ---------- Authentication ----------- //

    authService.getUserAuth.get(function(data) {
        if (data.userName) {
            $rootScope.authenticated = true;
            $rootScope.user = data;

        } else {
            $rootScope.authenticated = false;
        }
    }, function(error) {
           $rootScope.authenticated = false;
    });

    $scope.logout = function() {
         authService.logout.save(function(data) {
             $rootScope.authenticated = false;
             $window.location.href = '/';
         }, function(error) {
            $rootScope.authenticated = false;
            $window.location.href = '/';
         });
    };

     // --------------------------------------- //

}

function NavBarCtrl($scope, $rootScope, $window, navBarService, documentLookupService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    $scope.approvals = [];

    // Get all notifications for this user
    navBarService.approvals.query(function(data) {
        $scope.approvals = data;

        // Get all corresponding documents to align info with notification
        var documentIds = [];
        for (var i = 0; i < data.length; i++) {
            documentIds.push(data[i].documentId);
        }

        // Align docs to notification
        if (documentIds.length > 0) {
            documentLookupService.multiple.query({documentIds: documentIds}, function(documents) {
                $scope.matchDocumentToApproval(documents);
            }, function(error) {
                $scope.error = error;
            });
        }

    }, function(error) {
        $scope.error = error;
    });

    // Helper function to line up document with approval
    $scope.matchDocumentToApproval = function(documents) {
        var approvals = $scope.approvals;
        for (var i = 0; i < approvals.length; i++) {
            for (var j = 0; j < documents.length; j++) {
                if (approvals[i].documentId === documents[i].id) {
                    approvals[i].document = documents[i];
                }
            }
        }
        $scope.approvals = approvals;
    };

    // Approve revision notification
    $scope.approve = function(notification, i) {
        navBarService.approvals.approve({notificationId: notification.id}, function(data) {
            $scope.approvals.splice(i, 1);
        }, function(error) {
            $scope.error = error;
        })
    };

    $scope.deny = function(notification) {
// TODO
    };
}

function NewsFeedCtrl ($scope, $rootScope, $window, navBarService, documentLookupService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    $scope.approvals = [];

    // Get all notifications for this user
    navBarService.approvals.query(function(data) {
        $scope.approvals = data;

        // Get all corresponding documents to align info with notification
        var documentIds = [];
        for (var i = 0; i < data.length; i++) {
            documentIds.push(data[i].documentId);
        }

        // Align docs to notification
        if (documentIds.length > 0) {
            documentLookupService.multiple.query({documentIds: documentIds}, function(documents) {
                $scope.matchDocumentToApproval(documents);
            }, function(error) {
                $scope.error = error;
            });
        }

    }, function(error) {
        $scope.error = error;
    });

    // Helper function to line up document with approval
    $scope.matchDocumentToApproval = function(documents) {
        var approvals = $scope.approvals;
        for (var i = 0; i < approvals.length; i++) {
            for (var j = 0; j < documents.length; j++) {
                if (approvals[i].documentId === documents[i].id) {
                    approvals[i].document = documents[i];
                }
            }
        }
        $scope.approvals = approvals;
    };
}

angular
    .module('provesoft')
    .controller('MainCtrl', MainCtrl)
    .controller('NavBarCtrl', NavBarCtrl)
    .controller('NewsFeedCtrl', NewsFeedCtrl);