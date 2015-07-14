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

        allUsers: $resource('/resource/admin/user/all'),

        first10: $resource('/resource/admin/users/first10'),

        userProperties: $resource('/resource/admin/user/properties',
            {},
            {
                updatePrimaryOrg: {
                    method: 'PUT',
                    params: {
                        userId: '@userId',
                        primaryOrgId: '@primaryOrgId'
                    },
                    isArray: false
                },
                updateAltOrgs: {
                    method: 'PUT',
                    params: {
                        userId: '@userId',
                        altOrgId: '@altOrgId'
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
};

function documentTypeService ($resource) {

    return {
        documentType: $resource('/resource/admin/documentType')
    }
};

angular
    .module('provesoft')
    .factory('manageUsersService', manageUsersService)
    .factory('documentTypeService', documentTypeService);