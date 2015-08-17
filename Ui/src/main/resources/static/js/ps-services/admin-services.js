'use strict';

function manageUsersService($resource) {
    return {
        user: $resource('/resource/admin/user',
            {},
            {
                remove: {
                    method: 'DELETE',
                    params: {
                        userId: '@userId'
                    },
                    isArray: false
                }
            }
        ),

        userWildSearch: $resource('/resource/admin/user/wildSearch'),

        allUsers: $resource('/resource/admin/user/all'),

        first10: $resource('/resource/admin/users/first10'),

        userPrimaryOrg: $resource('/resource/admin/user/primaryOrg',
            {},
            {
                update: {
                    method: 'POST',
                    params: {
                        userId: '@userId'
                    },
                    isArray: false
                }
            }
        ),

        userProperties: $resource('/resource/admin/user/properties',
            {},
            {
                updateAltOrgs: {
                    method: 'PUT',
                    params: {
                        userId: '@userId',
                        altOrgIds: '@altOrgIds'
                    },
                    isArray: false
                },
                updateRoles: {
                    method: 'PUT',
                    params: {
                        userId: '@userId',
                        roleIds: '@roleIds'
                    },
                    isArray: false
                },
                deleteAltOrg: {
                    method: 'DELETE',
                    params: {
                        userId: '@userId',
                        orgId: '@orgId'
                    }
                },
                deleteRole: {
                    method: 'DELETE',
                    params: {
                        userId: '@userId',
                        roleId: '@roleId'
                    }
                }
            }
        ),

        userPermissions: $resource('/resource/admin/user/permissions',
            {},
            {
                save: {
                    method: 'POST',
                    params: {
                        userId: '@userId',
                        roleIds: '@roleIds'
                    },
                    isArray: true
                },
                remove: {
                    method: 'DELETE',
                    params: {
                        userId: '@userId',
                        roleId: '@roleId'
                    },
                    isArray: false
                }
            }
        ),

        allOrganizations: $resource('/resource/admin/organization/all'),

        organization: $resource('/resource/admin/organization'),

        allRoles: $resource('/resource/admin/role/all'),

        role: $resource('/resource/admin/role',
            {},
            {
                removeByRoleId: {
                    method: 'DELETE',
                    params: {
                        roleId: '@roleId'
                    },
                    isArray: false
                }
            }
        ),

        rolePermissions: $resource('/resource/admin/role/permissions',
            {},
            {
                save: {
                    method: 'POST',
                    params: {},
                    isArray: true
                }
            }
        ),

        orgUser: $resource('/resource/admin/orgUser',
            {},
            {
                save: {
                    method: 'POST',
                    params: {},
                    isArray: true
                },
                queryByUserId: {
                    method: 'GET',
                    params: {
                        userId: '@userId'
                    },
                    isArray: true
                },
                queryByOrganizationId: {
                    method: 'GET',
                    params: {
                        organizationId: '@organizationId'
                    },
                    isArray: true
                }
            }
        ),

        roleUser: $resource('/resource/admin/roleUser',
            {},
            {
                save: {
                    method: 'POST',
                    params: {},
                    isArray: true
                },
                queryByUserId: {
                    method: 'GET',
                    params: {
                        userId: '@userId'
                    },
                    isArray: true
                },
                queryByRoleId: {
                    method: 'GET',
                    params: {
                        roleId: '@roleId'
                    },
                    isArray: true
                }
            }
        ),

        /*
            Retrieve permissions by id
         */
        permissions: $resource('/resource/admin/permissions',
            {},
            {
                queryByUserId: {
                    method: 'GET',
                    params: {
                        userId: '@userId'
                    },
                    isArray: true
                },
                queryByRoleId: {
                    method: 'GET',
                    params: {
                        roleId: '@roleId'
                    },
                    isArray: true
                }
            }
        )

    }
}

function documentTypeService ($resource) {

    return {
        documentType: $resource('/resource/admin/documentType')
    }
}

function adminDocumentService($resource) {
    return {
        document: $resource('/resource/admin/document',
            {},
            {
                queryByState: {
                    method: 'GET',
                    params: {
                        state: '@state'
                    },
                    isArray: true
                }
            }
        )
    }
}

function adminApprovalService($resource) {
    return {
        approval: $resource('/resource/notifications/approvals',
            {},
            {
                override: {
                    method: 'PUT',
                    params: {
                        documentId: '@documentId',
                        stepId: '@stepId',
                        isTempStep: '@isTempStep'
                    },
                    isArray: false
                }
            }
        )
    }
}

function adminModuleSettingsService ($resource) {
    return {
        setting: $resource('/resource/admin/setting')
    }
}

function adminSignoffPathsService ($resource) {
    return {
        path: $resource('/resource/admin/signoffPath',
            {},
            {
                save: {
                    method: 'POST',
                    params: {
                        userId: '@userId'
                    },
                    isArray: false
                }
            }
        ),

        first10: $resource('/resource/admin/signoffPath/first10'),

        steps: $resource('/resource/admin/signoffPath/steps',
            {},
            {
                save: {
                    method: 'POST',
                    params: {},
                    isArray: true
                }
            }
        ),

        templateSteps: $resource('/resource/admin/signoffPath/steps/template',
            {},
            {
                save: {
                    method: 'POST',
                    params: {},
                    isArray: true
                },
                update: {
                    method: 'POST',
                    params: {},
                    isArray: true
                },
                remove: {
                    method: 'DELETE',
                    params: {
                        pathId: '@pathId',
                        stepIds: '@stepIds'
                    },
                    isArray: true
                }
            }
        )
    }
}

angular
    .module('provesoft')
    .factory('manageUsersService', manageUsersService)
    .factory('documentTypeService', documentTypeService)
    .factory('adminDocumentService', adminDocumentService)
    .factory('adminApprovalService', adminApprovalService)
    .factory('adminSignoffPathsService', adminSignoffPathsService)
    .factory('adminModuleSettingsService', adminModuleSettingsService);