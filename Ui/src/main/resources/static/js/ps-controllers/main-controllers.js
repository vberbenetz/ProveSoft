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

function NavBarCtrl($scope, navBarService, documentLookupService) {

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

function NewsFeedCtrl ($scope, navBarService, documentLookupService, userService, generalSettingsService) {

    $scope.isRedlineUsed = false;

    $scope.approvals = [];
    $scope.dailyFeed = [];

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

                // Align revisions to notification
                documentLookupService.revisions.query({documentIds: documentIds}, function(latestRevisions) {
                    $scope.matchLatestRevisionToApproval(latestRevisions);

                    // Get list of userIds to fetch user details
                    var userIds = [];
                    var approvals = $scope.approvals;
                    for (var i = 0; i < approvals.length; i++) {
                        userIds.push(approvals[i].revision.changeUserId);
                    }

                    // Align UserDetails to notification
                    userService.userDetails.queryByUserIds({userIds: userIds}, function(userDetails) {
                        $scope.matchUserDetailsToApproval(userDetails);

                    }, function(error) {
                        $scope.error = error;
                    });

                }, function(error) {
                    $scope.error = error;
                });

            }, function(error) {
                $scope.error = error;
            });
        }

    }, function(error) {
        $scope.error = error;
    });

    // Get all daily feed items
    documentLookupService.latestRevisionsForCompany.query(function(latestRevs) {

        // Change property "changeDate" to "date", required for sorting.
        // Get list of userIds to retrieve UserDetails for revision.
        var revUserIds = [];
        for (var z = 0; z < latestRevs.length; z++) {
            latestRevs[z].date = latestRevs[z].changeDate;
            revUserIds.push(latestRevs[z].changeUserId);
        }

        // Align UserDetails to notification
        userService.userDetails.queryByUserIds({userIds: revUserIds}, function(userDetails) {
            latestRevs = $scope.matchUserDetailsToRevision(userDetails, latestRevs);

            // Get latest comments for company
            documentLookupService.latestCommentsForCompany.query(function(latestComments) {

                var dailyFeed = latestRevs.concat(latestComments);

                // Sort the recent activity array by date to interleave the approvalHistory with the recentComments
                dailyFeed.sort(function(a, b) {
                    if (a.date > b.date) {
                        return -1;
                    }
                    if (a.date < b.date) {
                        return 1;
                    }
                    return 0;
                });

                // Format date
                $scope.dailyFeed = $scope.formatDate(dailyFeed);

            }, function(error) {
                $scope.error = error;
            });

        }, function(error) {
            $scope.error = error;
        });

    }, function(error) {
        $scope.error = error;
    });

    // Helper function to line up document with approval
    $scope.matchDocumentToApproval = function(documents) {
        var approvals = $scope.approvals;
        for (var i = 0; i < approvals.length; i++) {
            for (var j = 0; j < documents.length; j++) {
                if (approvals[i].documentId === documents[j].id) {
                    approvals[i].document = documents[j];
                }
            }
        }
        $scope.approvals = approvals;
    };

    // Helper function to line up revision with approval
    $scope.matchLatestRevisionToApproval = function(revisions) {
        if (revisions.length == 0) {
            return;
        }

        // Extract latest revisions
        var latestRevisions = [];
        for (var x = 1; x < revisions.length; x++) {
            if (revisions[x-1].key.documentId != revisions[x].key.documentId) {
                latestRevisions.push(revisions[x-1]);
            }

            // Add the last one to the list as this document has no one to compare against
            if (x == (revisions.length - 1) ) {
                latestRevisions.push(revisions[x]);
            }
        }

        var approvals = $scope.approvals;
        for (var i = 0; i < approvals.length; i++) {
            for (var j = 0; j < latestRevisions.length; j++) {
                if (approvals[i].documentId === latestRevisions[j].key.documentId) {
                    approvals[i].revision = latestRevisions[j];
                }
            }
        }
        $scope.approvals = approvals;
    };

    // Helper function to line up UserDetails with approval
    $scope.matchUserDetailsToApproval = function(userDetails) {
        var approvals = $scope.approvals;
        for (var i = 0; i < approvals.length; i++) {
            for (var j = 0; j < userDetails.length; j++) {
                if (approvals[i].revision.changeUserId === userDetails[j].userId) {
                    approvals[i].userDetails = userDetails[j];
                }
            }
        }
        $scope.approvals = approvals;
    };

    // Helper function to line up UserDetails with revision
    $scope.matchUserDetailsToRevision = function(userDetails, latestRevs) {
        for (var r = 0; r < latestRevs.length; r++) {
            for (var s = 0; s < userDetails.length; s++) {
                if (latestRevs[r].changeUserId === userDetails[s].userId) {
                    latestRevs[r].user = userDetails[s];
                }
            }
        }

        return latestRevs;
    };

    // Approve revision notification
    $scope.approve = function(notificationId, i) {
        navBarService.approvals.approve({notificationId: notificationId}, function(data) {
            $scope.approvals.splice(i, 1);
        }, function(error) {
            $scope.error = error;
        })
    };

    $scope.reject = function(notification) {
        var a = 23;
// TODO
    };

    $scope.formatDate = function(docActivity) {
        var currentDate = new Date();
        for (var i = 0; i < docActivity.length; i++) {

            var docDate = new Date(docActivity[i].date);

            if ( docDate.getFullYear()==currentDate.getFullYear() &&
                docDate.getMonth()==currentDate.getMonth() &&
                docDate.getDate()==currentDate.getDate()
            ) {
                docActivity[i].date = 'Today at '+ docDate.getHours() + ':' + docDate.getMinutes();
            }
        }

        return docActivity;
    }
}

angular
    .module('provesoft')
    .controller('MainCtrl', MainCtrl)
    .controller('NavBarCtrl', NavBarCtrl)
    .controller('NewsFeedCtrl', NewsFeedCtrl);