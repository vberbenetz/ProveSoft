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

        multiple: $resource('/resource/document/multiple',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        documentIds: '@documentIds'
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
        ),

        revisions: $resource('/resource/document/revisions',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        documentIds: '@documentIds'
                    },
                    isArray: true
                }
            }
        ),

        latestRevisions: $resource('/resource/document/revision/multiLatest',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        documentIds: '@documentIds'
                    },
                    isArray: true
                }
            }
        ),

        latestRevisionsForCompany: $resource('/resource/document/revisions/recent'),

        approvalHistory: $resource('/resource/approvalHistory',
            {},
            {
                queryByDocumentAndRevision: {
                    method: 'GET',
                    params: {
                        documentId: '@documentId',
                        revisionId: '@revisionId'
                    },
                    isArray: true
                }
            }
        ),

        recentApprovalHistory: $resource('/resource/approvalHistory/recent',
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
        ),

        documentComment: $resource('/resource/document/comment'),

        documentComments: $resource('/resource/document/comments',
            {},
            {
                queryRecent: {
                    method: 'GET',
                    params: {
                        documentId: '@documentId',
                        recent: true
                    },
                    isArray:true
                }
            }
        ),

        latestCommentsForCompany: $resource('/resource/document/comments/recent')
    }
}

function documentRevisionService($resource) {
    return {
        upload: $resource('/resource/upload',
            {},
            {
                remove: {
                    method: 'DELETE',
                    params: {
                        documentId: '@documentId',
                        tempRevId: '@tempRevId'
                    },
                    isArray: false
                }
            }
        ),

        revision: $resource('/resource/document/revision'),

        updateUploadRevisionId: $resource('/resource/upload/updateRevId',
            {},
            {
                update: {
                    method: 'PUT',
                    params: {
                        documentId: '@documentId',
                        tempRevId: '@tempRevId',
                        newRevId: '@newRevId'
                    },
                    isArray: false
                }
            }
        )
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

        templateSteps: $resource('/resource/signoffPath/steps/template',
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
        ),

        steps: $resource('/resource/signoffPath/steps',
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

angular
    .module('provesoft')
    .factory('documentCreationService', documentCreationService)
    .factory('documentLookupService', documentLookupService)
    .factory('documentRevisionService', documentRevisionService)
    .factory('signoffPathsService', signoffPathsService);