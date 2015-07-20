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

    $scope.editUser = {
        primaryOrg: '',
        altOrgs: [],
        roles: []
    };

    $scope.newOrg = {
        name: '',
        description: ''
    };

    $scope.editOrg = {
        newMember: {},
        newDescription: ''
    };

    $scope.newRole = {
        name: '',
        description: ''
    };

    $scope.newRolePermissions = [];

    $scope.permission = {
        organization: {},
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
    $scope.populatedUserDropdown = false;
    $scope.populatedOrgDropdown = false;
    $scope.populatedRoleDropdown = false;

    // Initialize notification variables
    $scope.successfullyAddedUser = false;
    $scope.successfullyModifiedUser = false;
    $scope.successfullyAddedOrg = false;
    $scope.successfullyAddedRole = false;
    $scope.successfullyModifiedRole = false;


    // Load all organizations
    manageUsersService.allOrganizations.query(function(orgs) {

        $scope.organizations = orgs;

        // Load preview of users
        manageUsersService.allUsers.query(function(users) {

            $scope.users = users;

            $scope.loadUsers();

        }, function(error) {
            $scope.err = error;
        });

    }, function(error) {
        $scope.err = error;
    });

    // Load all roles
    manageUsersService.allRoles.query(function(roles) {
        $scope.roles = roles;
        $scope.loadRoles();
    }, function(error) {
        $scope.err = error;
    });

    // ------------------ Methods -------------------- //

    // Need to load users twice for chosen dropdown watcher to register collection of options
    $scope.loadUsers = function() {
        if (!$scope.populatedUserDropdown) {
            manageUsersService.allUsers.query(function(users) {

                // Append organization name to user
                for (var i = 0; i < users.length; i++) {
                    users[i].primaryOrgName = $scope.getOrgNameById($scope.users[i].primaryOrgId);
                }

                $scope.users = users;
                $scope.populatedUserDropdown = true;

                $scope.loadOrgs();

            }, function(error) {
                $scope.err = error;
            });
        }
    };

    // Need to load orgs twice for chosen dropdown watcher to register collection of options
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

    // Need to load roles twice for chosen dropdown watcher to register collection of options
    $scope.loadRoles = function() {
        if (!$scope.populatedRoleDropdown) {
            manageUsersService.allRoles.query(function(roles) {
                $scope.roles = roles;
                $scope.populatedRoleDropdown = true;
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

                manageUsersService.roleUser.queryByUserId({userId: data.userId}, function(roleUsers) {

                    $scope.rightPanel.data.roles = $scope.getRoleListByRoleUsers(roleUsers);

                    manageUsersService.permissions.queryByUserId({userId: data.userId}, function(userPermissions) {
                        $scope.rightPanel.data.userPermissions = userPermissions;

                    }, function(error) {
                        $scope.err = error;
                    })

                }, function(error) {
                    $scope.err = error;
                });

            }, function(error) {
                $scope.err = error;
            });

            $scope.rightPanel.view = view;

        }
        else if (view === 'org') {
            $scope.rightPanel.data = data;

            manageUsersService.orgUser.queryByOrganizationId({orgId: data.organizationId}, function(orgUsers) {
                $scope.rightPanel.data.orgUsers = orgUsers;
            }, function(error) {
                $scope.err = error;
            });

            $scope.rightPanel.view = view;
        }
        else if (view === 'role') {

            manageUsersService.permissions.queryByRoleId({roleId: data.roleId}, function(rolePermissions) {
                $scope.rightPanel.data = {
                    role: data,
                    rolePermissions: rolePermissions
                };

                $scope.rightPanel.view = view;

            }, function(error) {
                $scope.err = error;
            });

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


    /* ----------- User Related ------------ */

    $scope.createNewUser = function() {

        var newUserRoles = [], newUserAdditionalOrgs = [], savedUser = {};

        // Deep copy arrays before deleting from object
        angular.copy($scope.newUser.additionalOrgs, newUserAdditionalOrgs);
        for (var i = 0; i < $scope.newUser.roles.length; i++) {
            newUserRoles.push($scope.newUser.roles[i].roleId);
        }

        // Format userDetails object
        $scope.newUser.primaryOrgId = $scope.newUser.primaryOrg.organizationId;
        delete $scope.newUser.primaryOrg;
        delete $scope.newUser.roles;
        delete $scope.newUser.additionalOrgs;

        manageUsersService.user.save($scope.newUser, function(data, status, headers, config) {
            savedUser = data;
            savedUser.primaryOrgName = $scope.getOrgNameById(savedUser.primaryOrgId);
            $scope.users.push(savedUser);

            manageUsersService.userPermissions.save({userId: savedUser.userId, roleIds: newUserRoles},
            function(data, status, headers, config) {

                var formattedAdditionalOrgs = $scope.genNewUserAdditionalOrgsPkg(newUserAdditionalOrgs, savedUser.userId);

                manageUsersService.orgUser.save(formattedAdditionalOrgs, function(data, status, headers, config) {

                    // Reset user model
                    $scope.newUser.firstName = '';
                    $scope.newUser.lastName = '';
                    $scope.newUser.email = '';
                    $scope.newUser.title = '';
                    $scope.newUser.roles = [];
                    $scope.newUser.primaryOrg = {};
                    $scope.newUser.additionalOrgs = [];

                    // Send out success alert notification
                    $scope.successfullyAddedUser = true;
                    setTimeout(function() {
                        $scope.$apply(function() {
                            $scope.successfullyAddedUser = false;
                        });
                    }, 2000);

                }, function(data, status, headers, config) {
                    $scope.err = status;

                    // Reset user model
                    $scope.newUser.firstName = '';
                    $scope.newUser.lastName = '';
                    $scope.newUser.email = '';
                    $scope.newUser.title = '';
                    $scope.newUser.roles = [];
                    $scope.newUser.primaryOrg = {};
                    $scope.newUser.additionalOrgs = [];
                });

            }, function(data, status, headers, config) {
                $scope.err = status;

                // Reset user model
                $scope.newUser.firstName = '';
                $scope.newUser.lastName = '';
                $scope.newUser.email = '';
                $scope.newUser.title = '';
                $scope.newUser.roles = [];
                $scope.newUser.primaryOrg = {};
                $scope.newUser.additionalOrgs = [];
            });

        }, function(data, status, headers, config) {
            $scope.err = status;

            // Reset user model
            $scope.newUser.firstName = '';
            $scope.newUser.lastName = '';
            $scope.newUser.email = '';
            $scope.newUser.title = '';
            $scope.newUser.roles = [];
            $scope.newUser.primaryOrg = {};
            $scope.newUser.additionalOrgs = [];
        });

    };

    $scope.updateUserPrimaryOrganization = function() {
        if (($scope.editUser.primaryOrg !== '') &&
            (typeof $scope.editUser.primaryOrg !== 'undefined') &&
            ($scope.editUser.primaryOrg !== null)
        ) {

            var userId = $scope.rightPanel.data.userId;
            var primaryOrgId = $scope.editUser.primaryOrg.organizationId;

            manageUsersService.userProperties.updatePrimaryOrg({userId: userId, primaryOrgId: primaryOrgId}, function(data) {

                // Reset primaryOrg edit ng-model
                $scope.editUser.primaryOrg = '';

                // Refresh front end user data
                $scope.rightPanel.data.primaryOrgId = primaryOrgId;
                $scope.rightPanel.data.primaryOrgName = $scope.getOrgNameById(primaryOrgId);

                // Send out success alert notification
                $scope.successfullyModifiedUser = true;
                setTimeout(function() {
                    $scope.$apply(function() {
                        $scope.successfullyModifiedUser = false;
                    });
                }, 2000);

            }, function(error) {
                $scope.err = error;
            });
        }
    };

    $scope.updateUserAlternateOrganizations = function() {
        if (($scope.editUser.altOrgs.length > 0 ) &&
            (typeof $scope.editUser.altOrgs !== 'undefined') &&
            ($scope.editUser.altOrgs != null)
        ) {

            var userId = $scope.rightPanel.data.userId;
            var altOrgs = $scope.editUser.altOrgs;
            var ids = [];

            for (var i = 0; i < altOrgs.length; i++) {
                ids.push(altOrgs[i].organizationId);
            }

            manageUsersService.userProperties.updateAltOrgs({userId: userId, altOrgId: ids}, function(data) {

                // Reset alternateOrg edit ng-model
                $scope.editUser.altOrgs = [];

                // Send out success alert notification
                $scope.successfullyModifiedUser = true;
                setTimeout(function() {
                    $scope.$apply(function() {
                        $scope.successfullyModifiedUser = false;
                    });
                }, 2000);

                // Refresh front end user data
                $scope.changeRightPanel('user', $scope.rightPanel.data);
            }, function(error) {
                $scope.err = error;
            });
        }

    };

    $scope.updateUserRoles = function() {
        if (($scope.editUser.roles.length > 0 ) &&
            (typeof $scope.editUser.roles !== 'undefined') &&
            ($scope.editUser.roles != null)
        ) {

            var userId = $scope.rightPanel.data.userId;
            var roles = $scope.editUser.roles;
            var ids = [];

            for (var i = 0; i < roles.length; i++) {
                ids.push(roles[i].roleId);
            }

            manageUsersService.userProperties.updateRoles({userId: userId, roleIds: ids}, function(data) {

                // Reset alternateRole edit ng-model
                $scope.editUser.roles = [];

                // Send out success alert notification
                $scope.successfullyModifiedUser = true;
                setTimeout(function() {
                    $scope.$apply(function() {
                        $scope.successfullyModifiedUser = false;
                    });
                }, 2000);

                // Refresh front end user data
                $scope.changeRightPanel('user', $scope.rightPanel.data);
            }, function(error) {
                $scope.err = error;
            });
        }
    };

    $scope.deleteUserAlternateOrganization = function(org) {
        var userId = $scope.rightPanel.data.userId;
        var orgId = org.organizationId;

        manageUsersService.userProperties.deleteAltOrg({userId: userId, orgId: orgId}, function(data) {
            // Refresh front end user data
            $scope.changeRightPanel('user', $scope.rightPanel.data);
        }, function(err) {
            $scope.error = err;
        });
    };

    $scope.deleteUserRole = function(role) {
        var userId = $scope.rightPanel.data.userId;
        var roleId = role.roleId;

        manageUsersService.userPermissions.remove({userId: userId, roleId: roleId}, function(data) {

            manageUsersService.userProperties.deleteRole({userId: userId, roleId: roleId}, function(data) {

                // Refresh front end user data
                var userRoles = $scope.rightPanel.data.roles;
                for (var i = 0; i < userRoles.length; i++) {
                    if (userRoles[i].roleId == roleId) {
                        userRoles.splice(i, 1);
                        break;
                    }
                }

            }, function(err) {
                $scope.error = err;
            });

        }, function(err) {
            $scope.error = err;
        });


    };

    $scope.deleteUser = function(user) {
        var userId = user.userId;

        manageUsersService.user.remove({userId: userId}, function(data) {
            var users = $scope.users;
            for (var i = 0; i < users.length; i++) {
                if (users[i].userId === userId) {
                    $scope.users.splice(i, 1);
                    break;
                }
            }

            // Clear right panel if user is there
            if ($scope.rightPanel.data.userId === userId) {
                $scope.rightPanel.data = {};
                $scope.rightPanel.view = 'new-user';
            }

        }, function(err) {
            $scope.error = err;
        });
    };


    /* ----------- Organization Related ------------ */

    $scope.createNewOrg = function() {

        manageUsersService.organization.save($scope.newOrg, function(data, status, headers, config) {
            $scope.newOrg = {
                name: '',
                description: ''
            };

            // Send out success alert notification
            $scope.successfullyAddedOrg = true;
            setTimeout(function() {
                $scope.$apply(function() {
                    $scope.successfullyAddedOrg = false;
                });
            }, 2000);

        }, function(data, status, headers, config) {
            $scope.err = status;
        });
    };

    $scope.addMember = function () {

        var organizationId = $scope.rightPanel.data.organizationId;
        var memberToAdd = [
            {
                key: {
                    organizationId: organizationId,
                    userId: $scope.editOrg.newMember.userId
                }
            }
        ];

        manageUsersService.orgUser.save(memberToAdd, function(data, status, headers, config) {

            // Add in newly added member
            $scope.rightPanel.data.orgUsers.push($scope.editOrg.newMember);

            // Reset new member ng-model
            $scope.editOrg.newMember = {};

        }, function(data, status, headers, config) {
            $scope.error = status;
        });
    };


    /* ----------- Role Related ------------ */

    /*
        Add the selected organization to the role template (Does not persist and only front-end)
     */
    $scope.addOrgToRole = function() {

        var permToAdd = $scope.permission;

        $scope.newRolePermissions.push({
            key: {
                organizationId: permToAdd.organization.organizationId
            },
            organizationName: permToAdd.organization.name,      // Temporary variable so user can see name (will be deleted on save)
            viewPerm: permToAdd.viewPerm,
            revisePerm: permToAdd.revisePerm,
            commentPerm: permToAdd.commentPerm,
            adminPerm: permToAdd.adminPerm
        });

        $scope.permission = {
            organization: {},
            viewPerm: true,
            revisePerm: false,
            commentPerm: false,
            adminPerm: false
        };
    };

    /*
        Remove select organization and its permissions from the role template (Does tno persist and only front-end)
     */
    $scope.removeOrgFromRole = function(index) {
        $scope.newRolePermissions.splice(index, 1);
    };

    /*
        Save the new Role and all the Role permissions to the backend
     */
    $scope.createNewRole = function() {

        manageUsersService.role.save($scope.newRole, function(addedRole, status, headers, config) {
            $scope.newRole = {
                name: '',
                description: ''
            };

            var newRolePerms = $scope.newRolePermissions;

            for (var i = 0; i < newRolePerms.length; i++) {
                newRolePerms[i].key.roleId = addedRole.roleId;
                delete newRolePerms[i].organizationName;        // Remove placeholder name for saving
            }
            $scope.newRolePermissions = newRolePerms;

            manageUsersService.rolePermissions.save($scope.newRolePermissions, function(data, status, headers, config) {
                $scope.newRolePermissions.splice(0, Number.MAX_VALUE);
                $scope.permission = {
                    organization: {},
                    viewPerm: true,
                    revisePerm: false,
                    commentPerm: false,
                    adminPerm: false
                };

                // Add newly created role to front-end list
                $scope.roles.push(addedRole);

                // Send out success alert notification
                $scope.successfullyAddedRole = true;
                setTimeout(function() {
                    $scope.$apply(function() {
                        $scope.successfullyAddedRole = false;
                    });
                }, 2000);

            }, function(data, status, headers, config) {
                $scope.err = status;
            });

        }, function(data, status, headers, config) {
            $scope.err = status;
        });

    };

    $scope.updateRolePermissions = function() {

        var updatedRolePermissions = $scope.rightPanel.data.rolePermissions;

        manageUsersService.rolePermissions.save(updatedRolePermissions, function(data, status, headers, config) {

            // Return removed organization name
            $scope.rightPanel.data.rolePermissions = data;

            // Send out success alert notification
            $scope.successfullyUpdatedRole = true;
            setTimeout(function() {
                $scope.$apply(function() {
                    $scope.successfullyUpdatedRole = false;
                });
            }, 2000);

        }, function(data, status, headers, config) {

        });
    };

    $scope.deleteRole = function(role) {
        var roleId = role.roleId;

        manageUsersService.role.removeByRoleId({roleId: roleId}, function(data) {

            var roles = $scope.roles;

            // Remove role from roles
            for (var i = 0; i < roles.length; i++) {
                if (roleId === roles[i].roleId) {
                    $scope.roles.splice(i, 1);
                    break;
                }
            }

        }, function(err) {
            $scope.error = err;
        })
    };


    /* ----------- Helpers ------------ */

    $scope.getOrgNameById = function(organizationId) {

        var orgs = $scope.organizations;
        for (var i = 0; i < orgs.length; i++) {
            if (orgs[i].organizationId == organizationId) {
                return orgs[i].name;
            }
        }
        return '';
    };

    $scope.getRoleListByRoleUsers = function(roleUsers) {

        var roles = $scope.roles;
        var rolesForUser = [];
        for (var i = 0; i < roleUsers.length; i++) {
            for (var j = 0; j < roles.length; j++) {
                if (roleUsers[i].key.roleId == roles[j].roleId) {
                    rolesForUser.push(roles[j]);
                }
            }
        }

        return rolesForUser;
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

function documentTypeSetupCtrl($scope, $rootScope, $window, documentTypeService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // New document type creation variables
    $scope.newDocumentType = {
        name: '',
        description: '',
        documentPrefix: '',
        maxNumberOfDigits: 1,
        startingNumber: 1
    };
    $scope.newNextDocumentId = '';

    // Hold generated next document Ids for document types
    $scope.nextDocumentId = [];

    // List of all existing document types
    $scope.documentTypes = [];


    // Get initial list of Document Types
    documentTypeService.documentType.query({}, function(data) {
        for (var i = 0; i < data.length; i++) {
            $scope.nextDocumentId[ data[i].id ] = $scope.generateNextDocId(data[i].documentPrefix, data[i].startingNumber, data[i].maxNumberOfDigits);
        }
        $scope.documentTypes = data;
    }, function(error) {
        $scope.err = error;
    });

    // Update document id preview
    $scope.$watch('newDocumentType',
        function(newVal, oldVal) {

            // Prevent overflowing of prefix string
            if (newVal.documentPrefix.length > 100) {
                newVal.documentPrefix = newVal.documentPrefix.substring(0, 99);
                $scope.documentPrefix = newVal.documentPrefix;
            }

            // Prevent overflowing the number
            if (newVal.maxNumberOfDigits > 15) {
                newVal.maxNumberOfDigits = 15;
                $scope.maxNumberOfDigits = newVal.maxNumberOfDigits;
            }

            // Prevent exceeding max digits
            if (newVal.startingNumber.toString().length > newVal.maxNumberOfDigits) {
                newVal.startingNumber = parseInt( newVal.startingNumber.toString().substring(0, newVal.maxNumberOfDigits - 1) );
                $scope.startingNumber = newVal.startingNumber;
            }

            $scope.newNextDocumentId = $scope.generateNextDocId(newVal.documentPrefix, newVal.startingNumber, newVal.maxNumberOfDigits);

        },
        true);


    // ------------------- Methods ------------------- //

    $scope.createNewDocumentType = function() {
        documentTypeService.documentType.save($scope.newDocumentType, function(data, status, headers, config) {

            for (var i = 0; i < data.length; i++) {
                $scope.nextDocumentId[ data[i].id ] = $scope.generateNextDocId(data[i].documentPrefix, data[i].startingNumber, data[i].maxNumberOfDigits);
            }

            $scope.documentTypes.push(data);

            // Reset newDocumentType
            $scope.newDocumentType = {
                name: '',
                description: '',
                documentPrefix: '',
                maxNumberOfDigits: '',
                startingNumber: ''
            }

        }, function(data, status, headers, config) {
            $scope.err = status;
        });
    };


    // --------------- Helpers ---------------- //

    $scope.generateNextDocId = function(documentPrefix, startingNumber, maxNumberOfDigits) {
        var prefix = documentPrefix;
        var suffix = startingNumber;

        for (var z = startingNumber.toString().length; z < maxNumberOfDigits; z++) {
            suffix = '0' + suffix;
        }
        return prefix + suffix;
    }

}

function moduleSettingsCtrl($scope, $rootScope, $window, adminModuleSettingsService, generalSettingsService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    generalSettingsService.setting.get({setting: 'redline'}, function(data) {
        $scope.redline = data.value;
    });

    // ---------------- Methods ----------------- //

    // Watch directive for when upload completes
    $scope.$watch('redline', function(newVal, oldVal) {
        if (newVal != oldVal) {

            var payload = {
                key: {
                    setting: 'redline'
                },
                value: $scope.redline
            };

            adminModuleSettingsService.setting.save(payload, function(data, status, headers, config) {

            }, function(data, status, headers, config) {

            });
        }
    });

}

angular
    .module('provesoft')
    .controller('manageUsersCtrl', manageUsersCtrl)
    .controller('documentTypeSetupCtrl', documentTypeSetupCtrl)
    .controller('moduleSettingsCtrl', moduleSettingsCtrl);