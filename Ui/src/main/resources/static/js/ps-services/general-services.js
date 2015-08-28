function navBarService($resource) {
    return {
        approvals: $resource('/resource/notifications/approvals',
            {},
            {
                approve: {
                    method: 'PUT',
                    params: {
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
        userDetails: $resource('/resource/userDetails',
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
                }
            }
        ),
        profilePictureByIds: $resource('/resource/user/profilePicByIds',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        userIds: '@userIds'
                    },
                    isArray: true
                }
            }
        )
    }
}

function commentLikeService($resource) {
    return {
        commentLike: $resource('/resource/comment/like',
            {},
            {
                save: {
                    method: 'POST',
                    params: {
                        documentCommentId: '@documentCommentId'
                    },
                    isArray: false
                }
            }
        ),

        countForComment: $resource('/resource/comment/like/count',
            {},
            {
                get: {
                    method: 'GET',
                    params: {
                        documentCommentId: '@documentCommentId'
                    },
                    isArray: false
                }
            }
        ),

        likesForCommmentList: $resource('/resource/comment/likes')
    }
}

angular
    .module('provesoft')
    .factory('navBarService', navBarService)
    .factory('commentLikeService', commentLikeService)
    .factory('userService', userService);
