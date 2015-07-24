'use strict';

function documentCreationService($resource) {
    return {
        document: $resource('/resource/document',
            {},
            {
                get: {
                    method: 'GET',
                    params: {
                        documentId: '@documentId'
                    },
                    isArray: false
                },
                addSignoffPath: {
                    method: 'PUT',
                    params: {
                        documentId: '@documentId',
                        signoffPathId: '@signoffPathId'
                    },
                    isArray: false
                }
            }
        ),

        documentType: $resource('/resource/documentType'),

        organization: $resource('/resource/organization'),

        organizations: $resource('/resource/organization/byList',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        orgIds: '@orgIds'
                    },
                    isArray: true
                }
            }
        )
    }
}

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
        ),

        first10: $resource('/resource/document/first10'),

        revision: $resource('/resource/document/revision',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        documentId: '@documentId'
                    },
                    isArray: true
                }
            }
        )
    }
}

function documentRevisionService($resource) {
    return {
        revision: $resource('/resource/document/revision')
    }
}

function signoffPathsService($resource) {
    return {
        path: $resource('/resource/signoffPath',
            {},
            {
                get: {
                    method: 'GET',
                    params: {
                        pathId: '@pathId'
                    },
                    isArray: false
                }
            }
        ),

        pathMulti: $resource('/resource/signoffPath/multi',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        orgId: '@orgId'
                    },
                    isArray: true
                }
            }
        ),

        steps: $resource('/resource/signoffPath/steps',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        pathId: '@pathId'
                    },
                    isArray: true
                }
            }
        )
    }
}

angular
    .module('provesoft')
    .factory('documentCreationService', documentCreationService)
    .factory('documentLookupService', documentLookupService)
    .factory('documentRevisionService', documentRevisionService)
    .factory('signoffPathsService', signoffPathsService);