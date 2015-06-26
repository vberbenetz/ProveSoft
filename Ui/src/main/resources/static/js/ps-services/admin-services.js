'use strict';

function manageUsersService($resource) {
    return {
        allUsers: $resource('/resource/admin/users/all'),

        user: $resource('/resource/admin/users/single',
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
                        roleId: '@roleId'
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
                        roleId: '@orgId'
                    }
                }
            }
        ),

        userDelete: $resource('/resource/admin/user/delete'),

        first10: $resource('/resource/admin/users/first10'),

        allOrganizations: $resource('/resource/admin/organizations/all'),

        organization: $resource('/resource/admin/organizations/single'),

        allRoles: $resource('/resource/admin/roles/all'),

        role: $resource('/resource/admin/roles/single',
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
        )

    }
};

angular
    .module('provesoft')
    .factory('manageUsersService', manageUsersService);