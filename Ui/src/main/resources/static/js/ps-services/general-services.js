function navBarService($resource) {
    return {
        approvals: $resource('/resource/notifications/approvals',
            {},
            {
                approve: {
                    method: 'PUT',
                    params: {
                        action: 'approve',
                        notificationId: '@notificationId'
                    },
                    isArray: false
                },
                reject: {
                    method: 'PUT',
                    params: {
                        action: 'reject',
                        notificationId: '@notificationId'
                    },
                    isArray: false
                }
            }
        )
    }
}

function userService($resource) {
    return {
        userDetails: $resource('/resource/user/details',
            {},
            {
                queryByUserIds: {
                    method: 'GET',
                    params: {
                        userIds: '@userIds'
                    },
                    isArray: true
                },
                getMe: {
                    method: 'GET',
                    isArray: false
                }
            }
        ),
        profilePicture: $resource('/resource/user/profilePic',
            {},
            {
                getPic: {
                    method: 'GET',
                    params: {},
                    isArray: false
                },
                query: {
                    method: 'GET',
                    params: {
                        userIds: '@userIds'
                    },
                    isArray: true
                }
            }
        ),
        pr: $resource('/resource/user/pr',
            {},
            {
                resetRequest: {
                    method: 'POST',
                    params: {},
                    isArray: false
                }
            }
        )
    }
}

function commentLikeService($resource) {
    return {
        documentCommentLike: $resource('/resource/document/comment/like',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        documentCommentIds: '@documentCommentIds'
                    },
                    isArray: true
                },
                save: {
                    method: 'POST',
                    params: {
                        documentCommentId: '@documentCommentId'
                    },
                    isArray: false
                }
            }
        )
    }
}

angular
    .module('provesoft')
    .factory('navBarService', navBarService)
    .factory('commentLikeService', commentLikeService)
    .factory('userService', userService);
