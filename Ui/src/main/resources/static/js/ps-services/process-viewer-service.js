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

        documentType: $resource('/resource/document/type'),

        organization: $resource('/resource/organization',
            {},
            {
                query: {
                    method: 'GET',
                    params: {},
                    isArray: true
                },
                queryByOrgIds: {
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
        document: $resource('/resource/document',
            {},
            {
                getByDocumentId: {
                    method: 'GET',
                    params: {
                        documentId: '@documentId',
                        isArray: false
                    }
                },
                queryByDocumentIds: {
                    method: 'GET',
                    params: {
                        documentIds: '@documentIds'
                    },
                    isArray: true
                },
                queryByOrganizationId: {
                    method: 'GET',
                    params: {
                        organizationId: '@organizationId'
                    },
                    isArray: true
                },
                queryBySearchString: {
                    method: 'GET',
                    params: {
                        searchString: '@searchString'
                    },
                    isArray: true
                },
                queryByCompany: {
                    method: 'GET',
                    params: {
                        all: true
                    },
                    isArray: true
                },
                queryFirst10: {
                    method: 'GET',
                    params: {},
                    isArray: true
                }
            }
        ),

        revision: $resource('/resource/document/revision',
            {},
            {
                queryByDocumentId: {
                    method: 'GET',
                    params: {
                        documentId: '@documentId'
                    },
                    isArray: true
                },
                queryByDocumentIds: {
                    method: 'GET',
                    params: {
                        documentIds: '@documentIds'
                    },
                    isArray: true
                },
                queryRecent: {
                    method: 'GET',
                    params: {},
                    isArray: true
                }
            }
        ),

        favourite: $resource('/resource/document/favourite',
            {},
            {
                query: {
                    method: 'GET',
                    params: {},
                    isArray: true
                },
                add: {
                    method: 'POST',
                    params: {},
                    isArray: false
                },
                remove: {
                    method: 'DELETE',
                    params: {
                        documentId: '@documentId'
                    },
                    isArray: false
                }
            }
        ),

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

        documentComment: $resource('/resource/document/comment',
            {},
            {
                queryRecentByDocumentId: {
                    method: 'GET',
                    params: {
                        documentId: '@documentId',
                        recent: true
                    },
                    isArray: true
                },
                queryRecent: {
                    method: 'GET',
                    params: {},
                    isArray: true
                }
            }
        ),

        childDocumentComment: $resource('/resource/document/comment/children',
            {},
            {
                query: {
                    method: 'GET',
                    params: {
                        parentCommentIds: '@parentCommentIds'
                    },
                    isArray: true
                }
            }
        )
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
                },
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