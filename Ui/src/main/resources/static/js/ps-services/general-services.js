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

angular
    .module('provesoft')
    .factory('navBarService', navBarService)
    .factory('userService', userService);
