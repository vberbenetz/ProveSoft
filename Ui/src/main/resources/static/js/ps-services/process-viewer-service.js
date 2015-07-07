'use strict';

function documentCreationService($resource) {
    return {
        document: $resource('/resource/document'),

        documentType: $resource('/resource/documentType'),

        organization: $resource('/resource/organization')
    }
};

function documentLookupService($resource) {
    return {
        lookup: $resource('/resource/document/lookup',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        searchString: '@searchString'
                    },
                    isArray: true
                }
            }
        )
    }
};

angular
    .module('provesoft')
    .factory('documentCreationService', documentCreationService)
    .factory('documentLookupService', documentLookupService);