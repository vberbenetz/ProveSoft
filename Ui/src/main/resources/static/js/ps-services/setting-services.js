'use strict';

function generalSettingsService($resource) {
    return {
        setting: $resource('/resource/setting',
            {},
            {
                method: 'GET',
                params: {
                    setting: '@setting'
                },
                isArray: false
            }
        )
    }
}

angular
    .module('provesoft')
    .factory('generalSettingsService', generalSettingsService);
