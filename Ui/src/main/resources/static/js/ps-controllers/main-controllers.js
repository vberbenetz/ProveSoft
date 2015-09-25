'use strict';

function MainCtrl($scope, $rootScope, $http, $window, authService, userService) {

    // ---------- Authentication ----------- //

    // Listen on route change to determine if user is logged in or needs redirect
    $rootScope.$on('$locationChangeStart', function() {
        $http.get('/user')
            .success(function(data) {
                if (typeof data.name === 'undefined') {
                    $window.location.href = '/';
                }
            })
            .error(function() {
                $window.location.href = '/';
            });
    });

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

    // Get my UserDetails
    userService.userDetails.getMe(function(myDetails) {
        $rootScope.userDetails = myDetails;
    }, function(error) {
        $rootScope.authenticated = false;
    });

    $scope.getProfilePicture = function() {
        userService.profilePicture.getPic(function(pic) {
            $scope.profilePicture = pic.picData;
        }, function(error) {
            $scope.error = error;
        });
    };

    // Get my profile picture
    $scope.profilePicture = null;
    $scope.getProfilePicture();

    // Watch for updates to profile picture via uploads
    $rootScope.$watch('profilePictureUpdated', function(newVal, oldVal) {
        if (newVal !== oldVal) {
            if (newVal) {
                $scope.getProfilePicture();
                $rootScope.profilePictureUpdated = false;
            }
        }
    });
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
            documentLookupService.document.queryByDocumentIds({documentIds: documentIds}, function(documents) {
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
                if (approvals[i].documentId === documents[j].id) {
                    approvals[i].document = documents[j];
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

function NewsFeedCtrl ($scope, $rootScope, navBarService, documentLookupService, userService, generalSettingsService, commentLikeService) {

    $scope.isRedlineUsed = false;

    $scope.documents = [];

    $scope.approvals = [];
    $scope.dailyFeed = [];
    $scope.favouriteDocuments = [];

    $scope.newFavouriteSelect = {
        selected: ''
    };

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

    // Get all documents for this company
    documentLookupService.document.queryByCompany(function(documents) {
        $scope.documents = documents;
    }, function(error) {
        $scope.error = error;
    });

    // Get all favourite documents for this user
    documentLookupService.favourite.query(function(favouriteDocuments) {
        $scope.favouriteDocuments = favouriteDocuments;
    }, function(error) {
        $scope.error = error;
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
            documentLookupService.document.queryByDocumentIds({documentIds: documentIds}, function(documents) {
                $scope.matchDocumentToApproval(documents);

                // Align revisions to notification
                documentLookupService.revision.queryByDocumentIds({documentIds: documentIds}, function(latestRevisions) {
                    $scope.matchLatestRevisionToApproval(latestRevisions);

                    // Get list of userIds to fetch user details and profile pics
                    var userIds = [];
                    var approvals = $scope.approvals;
                    for (var i = 0; i < approvals.length; i++) {
                        userIds.push(approvals[i].userId);
                    }

                    // Get all profile pictures
                    userService.profilePicture.query({userIds: userIds}, function(profilePictures) {
                       $scope.matchProfilePicToApproval(profilePictures);

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
    documentLookupService.revision.queryRecent(function(latestRevs) {

        // Get list of userIds to retrieve UserDetails for revision.
        var revUserIds = [];
        for (var z = 0; z < latestRevs.length; z++) {
            latestRevs[z].date = latestRevs[z].changeDate;
            revUserIds.push(latestRevs[z].changeUser.userId);
        }

        // Get list of documentIds to retrieve documents for revisions
        var dailyFeedDocumentIds = [];
        for (var y = 0; y < latestRevs.length; y++) {
            dailyFeedDocumentIds.push(latestRevs[y].key.documentId);
        }

        // Get latest comments for company
        documentLookupService.documentComment.queryRecent(function(latestComments) {

            // Append comment userIds for profile pic retrieval
            for (var y = 0; y < latestComments.length; y++) {
                revUserIds.push(latestComments[y].user.userId);
            }

            // Append comment documentIds for document retrieval
            for (var z = 0; z < latestComments.length; z++) {
                dailyFeedDocumentIds.push(latestComments[z].documentId);
            }

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

            // Get list of commentIds for comment like lookup
            var documentCommentIds = [];
            for (var k = 0; k < latestComments.length; k++) {
                documentCommentIds.push(latestComments[k].id);
            }

            // Append comment likes
            if (documentCommentIds.length > 0) {
                commentLikeService.documentCommentLike.query({documentCommentIds: documentCommentIds}, function(likes) {
                    $scope.matchLikesToComment(likes);
                }, function(error) {
                    $scope.error = error;
                });
            }

            // Match document to revision and comment
            if (dailyFeedDocumentIds.length > 0) {
                documentLookupService.document.queryByDocumentIds({documentIds: dailyFeedDocumentIds}, function(documents) {
                    $scope.matchDocumentsToDailyFeed(documents);
                }, function(error) {
                    $scope.error = error;
                });
            }

            // Retrieve profile pictures
            if (revUserIds.length > 0) {
                userService.profilePicture.query({userIds: revUserIds}, function(profilePictures) {
                    $scope.matchProfilePicToDailyFeed(profilePictures);

                }, function(error) {
                    $scope.error = error;
                });
            }

        }, function(error) {
            $scope.error = error;
        });

    }, function(error) {
        $scope.error = error;
    });

    // Add document to favourites
    $scope.addToFavourites = function() {
        var favToAdd = $scope.newFavouriteSelect.selected;
        if ( (typeof favToAdd !== 'undefined') || (favToAdd !== '') ) {
            documentLookupService.favourite.add($scope.newFavouriteSelect.selected, function (data, status, headers, config) {
                if (!$scope.checkIfFavouriteExists(data)) {
                    $scope.favouriteDocuments.push(data);
                    $scope.newFavouriteSelect = {};
                }
            }, function (data, status, headers, config) {
                $scope.newFavouriteSelect = {};
                $scope.error = status;
            });
        }
    };

    // Remove document from favourites
    $scope.removeFromFavourites = function(documentId) {
        documentLookupService.favourite.remove({documentId: documentId}, function(data) {
            var favouriteDocuments = $scope.favouriteDocuments;
            for (var i = 0; i < favouriteDocuments.length; i++) {
                if (favouriteDocuments[i].document.id === documentId) {
                    $scope.favouriteDocuments.splice(i, 1);
                    break;
                }
            }
        }, function(error) {
            $scope.error = error;
        });
    };


    // Helper function to check if new favourite needs to be added to frontend list
    $scope.checkIfFavouriteExists = function(newFav) {
        var favouriteDocuments = $scope.favouriteDocuments;
        for (var i = 0; i < favouriteDocuments.length; i++) {
            if (favouriteDocuments[i].document.id === newFav.document.id) {
                return true;
            }
        }

        return false;
    };

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
        if (revisions.length == 1) {
            latestRevisions.push(revisions[0]);
        }
        else {
            for (var x = 1; x < revisions.length; x++) {
                if (revisions[x-1].key.documentId != revisions[x].key.documentId) {
                    latestRevisions.push(revisions[x-1]);
                }

                // Add the last one to the list as this document has no one to compare against
                if (x == (revisions.length - 1) ) {
                    latestRevisions.push(revisions[x]);
                }
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

    // Helper function to line up Profile Picture with Approval
    $scope.matchProfilePicToApproval = function(profilePics) {
        var approvals = $scope.approvals;
        for (var i = 0; i < approvals.length; i++) {
            for (var j = 0; j < profilePics.length; j++) {
                if (approvals[i].revision.changeUser.userId === profilePics[j].userId) {
                    approvals[i].profilePicture = profilePics[j].picData;
                }
            }
        }
        $scope.approvals = approvals;
    };

    // Helper function to line up likes with document comment
    $scope.matchLikesToComment = function(likes) {
        var myUserId = $rootScope.userDetails.userId;

        var dailyFeed = $scope.dailyFeed;
        for (var f = 0; f < dailyFeed.length; f++) {

            // Only add likes for comments
            if (dailyFeed[f].hasOwnProperty('message')) {
                $scope.dailyFeed[f].numberOfLikes = 0;

                for (var g = 0; g < likes.length; g++) {
                    if (dailyFeed[f].id === likes[g].key.documentCommentId) {
                        $scope.dailyFeed[f].numberOfLikes++;

                        // Check if I liked it
                        if (likes[g].key.userId === myUserId) {
                            $scope.dailyFeed[f].iLikedIt = true;
                        }
                    }
                }
            }
        }
    };

    // Helper function to line up UserDetails with revision
    $scope.matchProfilePicToDailyFeed = function(profilePictures) {
        var dailyFeed = $scope.dailyFeed;
        for (var o = 0; o < dailyFeed.length; o++) {
            for (var p = 0; p < profilePictures.length; p++) {
                if (dailyFeed[o].hasOwnProperty('user') && (dailyFeed[o].user.userId === profilePictures[p].userId) ) {
                    $scope.dailyFeed[o].profilePicture = profilePictures[p].picData;
                }
                else if (dailyFeed[o].hasOwnProperty('changeUser') && dailyFeed[o].changeUser.userId === profilePictures[p].userId) {
                    $scope.dailyFeed[o].profilePicture = profilePictures[p].picData;
                }
            }
        }
    };

    //Helper function to match document to daily feed
    $scope.matchDocumentsToDailyFeed = function(documents) {
        var dailyFeed = $scope.dailyFeed;
        for (var i = 0; i < dailyFeed.length; i++) {
            for (var j = 0; j < documents.length; j++) {
                if ((dailyFeed[i].hasOwnProperty('message')) && (dailyFeed[i].documentId === documents[j].id)) {
                    $scope.dailyFeed[i].document = documents[j];
                    break;
                }
                if ((!dailyFeed[i].hasOwnProperty('message')) && (dailyFeed[i].key.documentId === documents[j].id)) {
                    $scope.dailyFeed[i].document = documents[j];
                    break;
                }
            }
        }
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
                docActivity[i].date = 'Today at '+ docDate.getHours() + ':' + (docDate.getMinutes()<10?'0':'') + docDate.getMinutes();
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