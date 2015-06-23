'use strict';

function manageUsersCtrl($scope, $rootScope, $window, manageUsersService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Form variables
    $scope.newUser = {
        firstName: '',
        lastName: '',
        email: '',
        roles: [],
        primaryOrg: {},
        additionalOrgs: []
    };

    $scope.newOrg = {
        name: '',
        description: ''
    };

    $scope.newRole = {
        viewPerm: true,
        revisePerm: false,
        commentPerm: false,
        adminPerm: false
    };

    // Set the starting view for the right panel
    $scope.rightPanel = {
        view: 'new-user',
        data: {}
    };

    // Initialize other variables
    $scope.organizations = [];


    // Load all organizations
    manageUsersService.allOrganizations.query(function(orgs) {

        $scope.organizations = orgs;

        // Load preview of users
        manageUsersService.allUsers.query(function(users) {

            $scope.users = users;

            // Append organization name to user
            for (var i = 0; i < users.length; i++) {
                for (var j = 0; j < orgs.length; i++) {

                    if (users[i].primaryOrgId === orgs[j].organizationId) {
                        $scope.users[i].primaryOrgName = orgs[j].name;
                        break;
                    }
                }
            }

        }, function(error) {
            $scope.err = error;
        });

    }, function(error) {
        $scope.err = error;
    });

    // Load all roles
    manageUsersService.allRoles.query(function(roles) {
        $scope.roles = roles;
    }, function(error) {
        $scope.err = error;
    });

    // ------------------ Methods -------------------- //

    // Initial org loading needed to populate organization dropdown under roles
    $scope.loadOrgs = function() {
        if (!$scope.populatedOrgDropdown) {
            manageUsersService.allOrganizations.query(function(orgs) {
                $scope.organizations = orgs;
                $scope.populatedOrgDropdown = true;
            }, function(error) {
                $scope.err = error;
            });
        }
    };

    $scope.changeRightPanel = function(view, data) {

        // Switch view and ignore if view name is incorrect
        if (view === 'user') {

            $scope.rightPanel.data = data;

            manageUsersService.orgUser.queryByUserId({userId: data.userId}, function(additionalOrgs) {
                $scope.rightPanel.data.additionalOrgs = additionalOrgs;
            }, function(error) {
                $scope.err = error;
            });

            manageUsersService.roleUser.queryByUserId({userId: data.userId}, function(roles) {
                var organizations = $scope.organizations;

                for (var i = 0; i < roles.length; i++) {
                    for (var j = 0; j < organizations.length; j++) {
                        if (roles[i].organizationId == organizations[j].organizationId) {
                            roles[i].organizationName = organizations[j].name;
                        }
                    }
                }

                $scope.rightPanel.data.roles = roles
            }, function(error) {
                $scope.err = error;
            });

            $scope.rightPanel.view = view;

        }
        else if ($scope.rightPanel.view === 'org') {

        }
        else if ($scope.rightPanel.view === 'role') {

        }
        else if (
            $scope.rightPanel.view === 'new_user' ||
            $scope.rightPanel.view === 'new_org' ||
            $scope.rightPanel.view === 'new_role'
        ) {
            $scope.rightPanel.view = view;
            $scope.rightPanel.data = data;
        }

    };

    $scope.createNewUser = function() {

        var newUserRoles = [], newUserAdditionalOrgs = [], savedUser = {};

        // Deep copy arrays before deleting from object
        angular.copy($scope.newUser.roles, newUserRoles);
        angular.copy($scope.newUser.additionalOrgs, newUserAdditionalOrgs);

        // Format userDetails object
        $scope.newUser.primaryOrgId = $scope.newUser.primaryOrg.organizationId;
        delete $scope.newUser.primaryOrg;
        delete $scope.newUser.roles;
        delete $scope.newUser.additionalOrgs;

        manageUsersService.user.save($scope.newUser, function(data, status, headers, config) {
            savedUser = data;
            savedUser.primaryOrgName = $scope.getOrgNameById(savedUser.primaryOrgId);
            $scope.users.push(savedUser);

            var formattedUserRoles = $scope.genNewUserRolesPkg(newUserRoles, savedUser.userId);

            manageUsersService.roleUser.save(formattedUserRoles, function(data, status, headers, config) {

                var formattedAdditionalOrgs = $scope.genNewUserAdditionalOrgsPkg(newUserAdditionalOrgs, savedUser.userId);

                manageUsersService.orgUser.save(formattedAdditionalOrgs, function(data, status, headers, config) {

                    // Reset user model
                    $scope.newUser.firstName = '';
                    $scope.newUser.lastName = '';
                    $scope.newUser.email = '';
                    $scope.newUser.roles = [];
                    $scope.newUser.primaryOrganization = {};
                    $scope.newUser.additionalOrgs = [];

                }, function(data, status, headers, config) {
                    $scope.err = status;

                    // Reset user model
                    $scope.newUser.firstName = '';
                    $scope.newUser.lastName = '';
                    $scope.newUser.email = '';
                    $scope.newUser.roles = [];
                    $scope.newUser.primaryOrganization = {};
                    $scope.newUser.additionalOrgs = [];
                });

            }, function(data, status, headers, config) {
                $scope.err = status;

                // Reset user model
                $scope.newUser.firstName = '';
                $scope.newUser.lastName = '';
                $scope.newUser.email = '';
                $scope.newUser.roles = [];
                $scope.newUser.primaryOrganization = {};
                $scope.newUser.additionalOrgs = [];
            });

        }, function(data, status, headers, config) {
            $scope.err = status;

            // Reset user model
            $scope.newUser.firstName = '';
            $scope.newUser.lastName = '';
            $scope.newUser.email = '';
            $scope.newUser.roles = [];
            $scope.newUser.primaryOrganization = {};
            $scope.newUser.additionalOrgs = [];
        });

    };

    $scope.createNewOrg = function() {

        manageUsersService.organization.save($scope.newOrg, function(data, status, headers, config) {
            $scope.newOrg = {};
        }, function(data, status, headers, config) {
            $scope.err = status;
        });
    };

    $scope.createNewRole = function() {

        $scope.newRole.organizationId = $scope.newRole.selectedOrg.organizationId;
        delete $scope.newRole.selectedOrg;

        if (typeof $scope.newRole.organizationId !== 'undefined') {

            manageUsersService.role.save($scope.newRole, function(data, status, headers, config) {
                $scope.newRole = {
                    viewPerm: true,
                    revisePerm: false,
                    commentPerm: false,
                    adminPerm: false
                };
            }, function(data, status, headers, config) {
                $scope.err = status;
            });
        }
    };

    $scope.formatRole = function(role) {
        role.organizationName = $scope.getOrgNameById(role.organizationId);
        return role;
    };

    $scope.getOrgNameById = function(organizationId) {

        var orgs = $scope.organizations;
        for (var i = 0; i < orgs.length; i++) {
            if (orgs[i].organizationId = organizationId) {
                return orgs[i].name;
            }
        }
        return '';
    };

    $scope.genNewUserRolesPkg = function(roles, userId) {

        var pkg = [];

        for (var i = 0; i < roles.length; i++) {
            pkg.push({
                key: {
                    roleId: roles[i].roleId,
                    userId: userId
                }
            })
        }

        return pkg;
    };

    $scope.genNewUserAdditionalOrgsPkg = function(orgs, userId) {

        var pkg = [];

        for (var i = 0; i < orgs.length; i++) {
            pkg.push({
                key: {
                    organizationId: orgs[i].organizationId,
                    userId: userId
                }
            })
        }

        return pkg;
    }


};

angular
    .module('provesoft')
    .controller('manageUsersCtrl', manageUsersCtrl);