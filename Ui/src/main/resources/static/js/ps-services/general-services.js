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

angular
    .module('provesoft')
    .factory('navBarService', navBarService);