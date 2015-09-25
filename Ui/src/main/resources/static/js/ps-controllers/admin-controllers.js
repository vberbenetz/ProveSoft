'use strict';

function manageUsersCtrl($scope, $rootScope, $window, $timeout, $modal, userService, manageUsersService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    // Form variables
    $scope.newUser = {
        firstName: '',
        lastName: '',
        email: '',
        title: '',
        roles: [],
        primaryOrganization: {},
        additionalOrgs: []
    };
    $scope.newUserValidationFail = {};

    $scope.userSearchString = '';
    $scope.noResultsFound = false;

    $scope.editUser = {
        primaryOrg: '',
        altOrgs: [],
        roles: []
    };

    $scope.newOrg = {
        name: '',
        description: ''
    };
    $scope.newOrgValidationFail = {};

    $scope.editOrg = {
        newMember: {},
        newDescription: ''
    };

    $scope.newRole = {
        name: '',
        description: ''
    };
    $scope.newRoleValidationFail = {};

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
        view: '',
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
    manageUsersService.organization.query(function(orgs) {

        $scope.organizations = orgs;

        // Load preview of users
        manageUsersService.user.queryAll(function(users) {
            $scope.users = users;

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

    $scope.changeRightPanel = function(view, data) {

        // Switch view and ignore if view name is incorrect
        if (view === 'user') {

            $scope.rightPanel.data = data;
            
            // Check if user is system admin
            manageUsersService.userIsAdmin.check({email: data.email}, function(result) {
                $scope.systemAdmin = result.isSystemAdmin;
            }, function(error) {
                $scope.error = error;
            });

            // Retrieve all user data
            manageUsersService.orgUser.queryByUserId({userId: data.userId}, function(additionalOrgs) {
                $scope.rightPanel.data.additionalOrgs = additionalOrgs;

                manageUsersService.roleUser.queryByUserId({userId: data.userId}, function(roleUsers) {

                    $scope.rightPanel.data.roles = $scope.getRoleListByRoleUsers(roleUsers);

                    manageUsersService.permissions.queryByUserId({userId: data.userId}, function(userPermissions) {
                        $scope.rightPanel.data.userPermissions = userPermissions;

                        // Retrieve profile pictures
                        var userIds = [data.userId];
                        userService.profilePicture.query({userIds: userIds}, function(profilePictures) {

                            if (profilePictures.length > 0) {
                                $scope.rightPanel.data.profilePicture = profilePictures[0].picData;
                            }

                        }, function(error) {
                            $scope.error = error;
                        });

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

    $scope.$watch('userSearchString', function(newVal, oldVal) {
        $scope.executeUserSearch();
    });


    /* ---------- Controller Methods ----------- */

    $scope.executeUserSearch = function() {
        $timeout( function() {

            // Run if search strings are different
            if ($scope.userSearchString !== $scope.prevUserSearchString) {
                $scope.prevUserSearchString = $scope.userSearchString;

                manageUsersService.user.queryBySearchString({name: $scope.userSearchString}, function(data, status, headers, config) {
                    if (data.length > 0) {
                        $scope.noResultsFound = false;
                        $scope.users = data;
                    }
                    else {
                        $scope.users.length = 0;    // Clear previous search results
                        $scope.noResultsFound = true;
                    }

                }, function(data, status, headers, config) {
                    $scope.error = status;
                });
            }
        } , 500);
    };

    $scope.validateNewUserForm = function(callback) {

        var validationFail = false;
        var firstname = $scope.newUser.firstName;
        var lastname = $scope.newUser.lastName;
        var email = $scope.newUser.email;
        var title = $scope.newUser.title;
        var roles = $scope.newUser.roles;
        var primaryOrganization = $scope.newUser.primaryOrganization;

        var alphaRegex = /^[a-zA-z]+$/i;
        var emailRegex = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;

        // Reset form validation error messages
        $scope.newUserValidationFail = {};

        // First name
        if ( (typeof firstname === 'undefined') || (firstname === '') || (firstname.length == 0) ) {
            $scope.newUserValidationFail.firstname = 'Please enter a first name';
            validationFail = true;
        }
        else if (firstname.length > 250) {
            $scope.newUserValidationFail.firstname = 'Please enter a valid first name under 250 characters';
            validationFail = true;
        }
        else if (!alphaRegex.test(firstname)) {
            $scope.newUserValidationFail.firstname = 'Please use only letters';
            validationFail = true;
        }

        // Last name
        if ( (typeof lastname === 'undefined') || (lastname === '') || (lastname.length == 0) ) {
            $scope.newUserValidationFail.lastname = 'Please enter a last name';
            validationFail = true;
        }
        else if (lastname.length > 250) {
            $scope.newUserValidationFail.lastname = 'Please enter a valid last name under 250 characters';
            validationFail = true;
        }
        else if (!alphaRegex.test(lastname)) {
            $scope.newUserValidationFail.lastname = 'Please use only letters';
            validationFail = true;
        }

        // Title
        if ( (typeof title === 'undefined') || (title === '') || (title.length == 0) ) {
            $scope.newUserValidationFail.title = 'Please enter a title';
            validationFail = true;
        }
        else if (title.length > 250) {
            $scope.newUserValidationFail.title = 'Please enter a title with fewer than 250 characters';
            validationFail = true;
        }

        // Role
        if ( (typeof roles === 'undefined') || (roles.length == 0) ) {
            $scope.newUserValidationFail.roles = 'Please choose at least one role';
            validationFail = true;
        }

        // Primary organization
        if ( (typeof primaryOrganization === 'undefined') || (Object.getOwnPropertyNames(primaryOrganization).length === 0) ) {
            $scope.newUserValidationFail.primaryOrganization = 'Please select a primary organization';
            validationFail = true;
        }

        // Email
        if ( (typeof email === 'undefined') || (email === '') || (email.length == 0) ) {
            $scope.newUserValidationFail.email = 'Please enter an email';
            validationFail = true;
        }
        else if (email.length > 250) {
            $scope.newUserValidationFail.email = 'Please enter a valid email under 250 characters';
            validationFail = true;
        }
        else if (!emailRegex.test(email)) {
            $scope.newUserValidationFail.email = 'Please enter a valid email';
            validationFail = true;
        }

        // Perform email exists check if email is valid above
        if (typeof $scope.newUserValidationFail.email === 'undefined') {
            manageUsersService.userExists.check({email: email}, function(result) {
                if (result.exists) {
                    $scope.newUserValidationFail.email = 'Email is already in use';
                    callback(false);
                }
                else {
                    callback(!validationFail);
                }
            }, function(error) {
                $scope.newUserValidationFail.email = 'Error verifying email';
                callback(false);
            });
        }
        else {
            callback(!validationFail);
        }
    };

    $scope.createNewUser = function() {

        var a = $scope.validateNewUserForm();

        $scope.validateNewUserForm(function(result) {

            if (result) {

                var newUserRoles = [], newUserAdditionalOrgs = [], savedUser = {};

                // Deep copy arrays before deleting from object
                angular.copy($scope.newUser.additionalOrgs, newUserAdditionalOrgs);
                for (var i = 0; i < $scope.newUser.roles.length; i++) {
                    newUserRoles.push($scope.newUser.roles[i].roleId);
                }

                // Format userDetails object
                delete $scope.newUser.roles;
                delete $scope.newUser.additionalOrgs;

                manageUsersService.user.save($scope.newUser, function (data, status, headers, config) {
                    savedUser = data;
                    $scope.users.push(savedUser);

                    manageUsersService.userPermissions.save({userId: savedUser.userId, roleIds: newUserRoles},
                        function (data, status, headers, config) {

                            var formattedAdditionalOrgs = $scope.genNewUserAdditionalOrgsPkg(newUserAdditionalOrgs, savedUser.userId);

                            manageUsersService.orgUser.save(formattedAdditionalOrgs, function (data, status, headers, config) {

                                // Reset user model
                                $scope.newUser.firstName = '';
                                $scope.newUser.lastName = '';
                                $scope.newUser.email = '';
                                $scope.newUser.title = '';
                                $scope.newUser.roles = [];
                                $scope.newUser.primaryOrganization = {};
                                $scope.newUser.additionalOrgs = [];

                                // Send out success alert notification
                                $scope.successfullyAddedUser = true;
                                setTimeout(function () {
                                    $scope.$apply(function () {
                                        $scope.successfullyAddedUser = false;
                                    });
                                }, 2000);

                            }, function (data, status, headers, config) {
                                $scope.err = status;

                                // Reset user model
                                $scope.newUser.firstName = '';
                                $scope.newUser.lastName = '';
                                $scope.newUser.email = '';
                                $scope.newUser.title = '';
                                $scope.newUser.roles = [];
                                $scope.newUser.primaryOrganization = {};
                                $scope.newUser.additionalOrgs = [];
                            });

                        }, function (data, status, headers, config) {
                            $scope.err = status;

                            // Reset user model
                            $scope.newUser.firstName = '';
                            $scope.newUser.lastName = '';
                            $scope.newUser.email = '';
                            $scope.newUser.title = '';
                            $scope.newUser.roles = [];
                            $scope.newUser.primaryOrganization = {};
                            $scope.newUser.additionalOrgs = [];
                        });

                }, function (data, status, headers, config) {
                    $scope.err = status;

                    // Reset user model
                    $scope.newUser.firstName = '';
                    $scope.newUser.lastName = '';
                    $scope.newUser.email = '';
                    $scope.newUser.title = '';
                    $scope.newUser.roles = [];
                    $scope.newUser.primaryOrganization = {};
                    $scope.newUser.additionalOrgs = [];
                });
            }
        })
    };

    $scope.updateUserPrimaryOrganization = function() {
        if ((Object.getOwnPropertyNames($scope.editUser.primaryOrganization).length !== 0) &&
            (typeof $scope.editUser.primaryOrganization !== 'undefined') &&
            ($scope.editUser.primaryOrganization !== null)
        ) {

            var userId = $scope.rightPanel.data.userId;
            var newPrimaryOrg = $scope.editUser.primaryOrganization;

            manageUsersService.userPrimaryOrg.update({userId: userId}, newPrimaryOrg, function(data) {

                // Reset primaryOrg edit ng-model
                $scope.editUser.primaryOrganization = {};

                // Refresh front end user data
                $scope.rightPanel.data = data;

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

            manageUsersService.userProperties.updateAltOrgs({userId: userId, altOrgIds: ids}, function(data) {

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

    $scope.updateUserSysAdminStatus = function() {
        if ($scope.rightPanel.data.email !== $rootScope.user.userName) {
            manageUsersService.userIsAdmin.update({email: $scope.rightPanel.data.email, updatedValue: $scope.systemAdmin}, function(data) {
                // Send out success alert notification
                $scope.successfullyModifiedUser = true;
                setTimeout(function() {
                    $scope.$apply(function() {
                        $scope.successfullyModifiedUser = false;
                    });
                }, 2000);
            }, function(error) {
                $scope.error = error;
            });
        }
        else {
            // Send out fail alert notification
            $scope.errorSelfAdminStatusModify = true;
            setTimeout(function() {
                $scope.$apply(function() {
                    $scope.errorSelfAdminStatusModify = false;
                });
            }, 2000);
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
            if (err.data.errorCode === 1) {
                var errorTitle = 'User Deletion Error';
                var errorMsg = 'You cannot delete yourself from this panel.';
                $scope.openAdminErrorModal(errorTitle, errorMsg);
            }
            else if (err.data.errorCode === 2) {
                var errorTitle = 'User Deletion Error';
                var errorMsg = 'You cannot delete another administrator. Please demote them to a regular user before proceeding.';
                $scope.openAdminErrorModal(errorTitle, errorMsg);
            }
        });
    };


    /* ----------- Organization Related ------------ */

    $scope.validateNewOrgForm = function() {

        var validationFail = false;
        var name = $scope.newOrg.name;
        var description = $scope.newOrg.description;

        // Reset form validation error messages
        $scope.newOrgValidationFail = {};

        if ( (typeof name === 'undefined') || (name === '') || (name.length == 0) ) {
            $scope.newOrgValidationFail.name = true;
            validationFail = true;
        }
        if ( (typeof description === 'undefined') || (description === '') || (description.length == 0) ) {
            $scope.newOrgValidationFail.description = true;
            validationFail = true;
        }

        return !validationFail;
    };

    $scope.createNewOrg = function() {

        if ($scope.validateNewOrgForm()) {

            manageUsersService.organization.save($scope.newOrg, function(data, status, headers, config) {
                $scope.organizations.push(data);

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
        }
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

    // Deletes an organization if it is not referenced by any other objects
    $scope.deleteOrganization = function(organization) {
        manageUsersService.organization.remove({organizationId: organization.organizationId}, function(data) {
            if (data.deleted) {
                $scope.removeOrganizationFromList(organization.organizationId);
            }
        }, function(error) {
            if ( (typeof error.data.deleted !== 'undefined') && (!error.data.deleted) ) {
                var errorTitle = 'Organization Deletion Error';
                var errorMsg = 'Could not delete Organization ' + organization.name + ' because this organization is referenced elsewhere.';
                $scope.openAdminErrorModal(errorTitle, errorMsg);
            }
            else {
                $scope.error = error;
            }
        });
    };


    /* ----------- Role Related ------------ */

    /*
        Add the selected organization to the role template (Does not persist and only front-end)
     */
    $scope.addOrgToRole = function() {

        var permToAdd = $scope.permission;

        // Check if user selected an organization before adding to permissions
        if (Object.getOwnPropertyNames(permToAdd.organization).length !== 0) {

            // Check if organization has been added to role already
            var addedPerms = $scope.newRolePermissions;
            for (var i = 0; i < addedPerms.length; i++) {
                if (addedPerms[i].key.organizationId === permToAdd.organization.organizationId) {
                    $scope.permission.organization = {};
                    return;
                }
            }

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
        }
    };

    /*
        Remove select organization and its permissions from the role template (Does not persist and only front-end)
     */
    $scope.removeOrgFromRole = function(index) {
        $scope.newRolePermissions.splice(index, 1);
    };

    $scope.validateNewRoleForm = function() {

        var validationFail = false;
        var name = $scope.newRole.name;
        var description = $scope.newRole.description;
        var permissions = $scope.newRolePermissions;

        // Reset form validation error messages
        $scope.newRoleValidationFail = {};

        if ( (typeof name === 'undefined') || (name === '') || (name.length == 0) ) {
            $scope.newRoleValidationFail.name = true;
            validationFail = true;
        }
        if ( (typeof description === 'undefined') || (description === '') || (description.length == 0) ) {
            $scope.newRoleValidationFail.description = true;
            validationFail = true;
        }
        if ( (typeof permissions === 'undefined') || (permissions.length == 0) ) {
            $scope.newRoleValidationFail.permissions = true;
            validationFail = true;
        }

        return !validationFail;
    };

    /*
        Save the new Role and all the Role permissions to the backend
     */
    $scope.createNewRole = function() {

        if ($scope.validateNewRoleForm()) {

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
        }

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

    // Deletes a Role if it is not referenced by any other objects
    $scope.deleteRole = function(role) {
        manageUsersService.role.remove({roleId: role.roleId}, function(data) {
            if (data.deleted) {
                $scope.removeRoleFromList(role.roleId);
            }
        }, function(error) {
            if ( (typeof error.data.deleted !== 'undefined') && (!error.data.deleted) ) {
                var errorTitle = 'Role Deletion Error';
                var errorMsg = 'Could not delete Role ' + role.name + ' because this role is referenced elsewhere.';
                $scope.openAdminErrorModal(errorTitle, errorMsg);
            }
            else {
                $scope.error = error;
            }
        });
    };


    /* ----------- Helpers ------------ */

    $scope.openAdminErrorModal = function(errorTitle, errorMsg) {
        var modalInstance = $modal.open({
            templateUrl:'views/admin/adminErrorModal.html',
            controller: adminErrorModalCtrl,
            size: 'sm',
            resolve: {
                errorTitle: function() {
                    return errorTitle;
                },
                errorMsg: function() {
                    return errorMsg;
                }
            }
        })
    };

    $scope.removeOrganizationFromList = function(organizationId) {
        var organizations = $scope.organizations;
        for (var i = 0; i < organizations.length; i++) {
            if (organizations[i].organizationId === organizationId) {
                $scope.organizations.splice(i, 1);
                break;
            }
        }
    };

    $scope.removeRoleFromList = function(roleId) {
        var roles = $scope.roles;
        for (var i = 0; i < roles.length; i++) {
            if (roles[i].roleId === roleId) {
                $scope.roles.splice(i, 1);
                break;
            }
        }
    };

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


}

function documentTypeSetupCtrl($scope, $rootScope, $window, $modal, documentTypeService) {

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
    $scope.fieldValidationFail = {};

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
                if ( (newVal.maxNumberOfDigits == '') || (newVal.maxNumberOfDigits == 0) ) {
                    $scope.maxNumberOfDigits = 1;
                    $scope.startingNumber = 1;
                }
                else {
                    newVal.startingNumber = parseInt( newVal.startingNumber.toString().substring(0, newVal.maxNumberOfDigits - 1) );
                    $scope.startingNumber = newVal.startingNumber;
                }
            }

            $scope.newNextDocumentId = $scope.generateNextDocId(newVal.documentPrefix, newVal.startingNumber, newVal.maxNumberOfDigits);

        },
        true);


    // ------------------- Methods ------------------- //

    $scope.validateNewDocumentTypeForm = function() {

        var validationFail = false;
        var name = $scope.newDocumentType.name;
        var description = $scope.newDocumentType.description;
        var docPrefix = $scope.newDocumentType.documentPrefix;

        // Reset form validation error messages
        $scope.fieldValidationFail = {};

        if ( (typeof name === 'undefined') || (name === '') || (name.length == 0) ) {
            $scope.fieldValidationFail.name = true;
            validationFail = true;
        }
        if ( (typeof description === 'undefined') || (description === '') || (description.length == 0) ) {
            $scope.fieldValidationFail.description = true;
            validationFail = true;
        }
        if ( (typeof docPrefix === 'undefined') || (docPrefix === '') || (docPrefix.length == 0) ) {
            $scope.fieldValidationFail.documentPrefix = true;
            validationFail = true;
        }

        return !validationFail;
    };

    $scope.createNewDocumentType = function() {

        if ($scope.validateNewDocumentTypeForm()) {

            documentTypeService.documentType.save($scope.newDocumentType, function(data, status, headers, config) {

                $scope.nextDocumentId[ data.id ] = $scope.generateNextDocId(data.documentPrefix, data.startingNumber, data.maxNumberOfDigits);
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
        }
    };

    $scope.removeDocumentType = function(documentType) {

        documentTypeService.documentType.remove({documentTypeId: documentType.id}, function(data) {
            if (data.deleted) {
                $scope.removeDocumentTypeFromList(documentType.id);
            }
        }, function(error) {
            if ( (typeof error.data.deleted !== 'undefined') && (!error.data.deleted) ) {
                var errorTitle = 'DocumentType Deletion Error';
                var errorMsg = 'Could not delete DocumentType ' + documentType.name + ' because documents exist for this type.';
                $scope.openDocumentTypeModal(errorTitle, errorMsg);
            }
            else {
                $scope.error = error;
            }
        });
    };

    $scope.openDocumentTypeModal = function(errorTitle, errorMsg) {
        var modalInstance = $modal.open({
            templateUrl:'views/admin/adminErrorModal.html',
            controller: adminErrorModalCtrl,
            size: 'sm',
            resolve: {
                errorTitle: function() {
                    return errorTitle;
                },
                errorMsg: function() {
                    return errorMsg;
                }
            }
        })
    };

    // --------------- Helpers ---------------- //

    $scope.generateNextDocId = function(documentPrefix, startingNumber, maxNumberOfDigits) {
        var prefix = documentPrefix;
        var suffix = startingNumber;

        for (var z = startingNumber.toString().length; z < maxNumberOfDigits; z++) {
            suffix = '0' + suffix;
        }
        return prefix + suffix;
    };

    $scope.removeDocumentTypeFromList = function(documentTypeId) {
        var documentTypes = $scope.documentTypes;
        for (var i = 0; i < documentTypes.length; i++) {
            if (documentTypes[i].id === documentTypeId) {
                $scope.documentTypes.splice(i, 1);
                break;
            }
        }
    }

}

function signoffPathsSetupCtrl ($scope, $rootScope, $window, $modal, manageUsersService, signoffPathsService, adminSignoffPathsService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    $scope.paths = [];

    $scope.rightPanel = {};
    $scope.showRightPanel = false;

    // Flag in new path creation. True if user wants to allow path to be used by all organizations
    $scope.toggleAllOrgs = false;

    $scope.newPath = {
        name: '',
        organization: {},
        applyToAll: false,
        initialApprover: {}
    };
    $scope.newPathValidationFail = {};

    $scope.initialApprover = {};

    $scope.newSteps = [];
    $scope.stepsToRemove = [];

    manageUsersService.user.queryAll(function(users) {
        $scope.users = users;
    }, function(error) {
        $scope.err = error;
    });

    manageUsersService.organization.query(function(orgs) {
        $scope.organizations = orgs;
    }, function(error) {
        $scope.err = error;
    });

    adminSignoffPathsService.first10.query(function(paths) {
        $scope.paths = paths;
    }, function(error) {
        $scope.err = error;
    });


    // ------------------ Methods -------------------- //

    $scope.changeRightPanel = function(path) {

        $scope.rightPanel = {};
        $scope.rightPanel.path = path;

        $scope.newSteps.length = 0;                 // Clear array
        $scope.stepsToRemove.length = 0;

        signoffPathsService.templateSteps.query({pathId: path.key.pathId}, function(steps) {
            $scope.rightPanel.steps = steps;
            $scope.showRightPanel = true;
        }, function(error) {
            $scope.err = error;
        });
    };

    $scope.validateCreateNewPath = function() {

        var validationFail = false;
        var name = $scope.newPath.name;
        var org = $scope.newPath.organization;
        var initApp = $scope.initialApprover;

        // Reset form validation error messages
        $scope.newPathValidationFail = {};

        if ( (typeof name === 'undefined') || (name === '') || (name.length == 0) ) {
            $scope.newPathValidationFail.name = true;
            validationFail = true;
        }
        if ( (!$scope.toggleAllOrgs) && ((typeof org === 'undefined') || (Object.getOwnPropertyNames(org).length === 0)) ) {
            $scope.newPathValidationFail.organization = true;
            validationFail = true;
        }
        if ( (typeof initApp === 'undefined') || (Object.getOwnPropertyNames(initApp).length === 0) ) {
            $scope.newPathValidationFail.initialApprover = true;
            validationFail = true;
        }

        return !validationFail;
    };

    $scope.createNewPath = function() {
        var initialApproverId = $scope.newPath.initialApprover.userId;
        delete $scope.newPath.initialApprover;

        // If applying to all, attach a placeholder organization
        if ($scope.newPath.applyToAll) {
            $scope.newPath.organization = $scope.organizations[0];
        }

        adminSignoffPathsService.path.save({userId: initialApproverId}, $scope.newPath, function(data, status, headers, config) {
            $scope.paths.push(data);

            // Reset new path variable
            $scope.newPath.name = '';
            $scope.newPath.organization = {};
            $scope.newPath.initialApprover = {};

        }, function(data, status, headers, config) {
            $scope.err = status;
            $scope.newPath.initialApprover = {};
        });
    };

    $scope.addStep = function() {
        $scope.newSteps.push({
            pathId: $scope.rightPanel.path.key.pathId,
            action: '',
            user: {}
        });
    };

    $scope.validateNewSteps = function() {
        return true;
    };

    $scope.saveChanges = function() {
        if ($scope.validateNewSteps()) {

            // Delete steps
            // Extract stepId's from steps to delete
            if ($scope.stepsToRemove.length > 0) {
                var stepIds = $scope.extractStepIds($scope.stepsToRemove);

                adminSignoffPathsService.templateSteps.remove({pathId: $scope.rightPanel.path.key.pathId, stepIds: stepIds}, function(data) {
                    $scope.stepsToRemove.length = 0;
                }, function(error) {
                    $scope.error = error;
                });
            }

            // Add steps if any were created
            if ($scope.newSteps.length > 0) {
                adminSignoffPathsService.templateSteps.save($scope.newSteps, function(data, status, headers, config) {
                    $scope.rightPanel.steps = $scope.rightPanel.steps.concat(data);
                    $scope.newSteps.length = 0;                 // Clear array
                }, function(data, status, headers, config) {
                    $scope.error = status;
                });
            }

            // Update existing steps if new user was selected
            for (var i = 0; i < $scope.rightPanel.steps.length; i++) {
                delete $scope.rightPanel.steps[i].edit;
            }
            adminSignoffPathsService.templateSteps.update($scope.rightPanel.steps, function(data, status, headers, config) {
            }, function(data, status, headers, config) {
                $scope.error = status;
            });

        }
    };

    $scope.deleteSignoffPath = function(signoffPath) {
        adminSignoffPathsService.path.remove({pathId: signoffPath.key.pathId}, function(data) {
            if (data.deleted) {
                $scope.removeSignoffPathFromList(signoffPath.key.pathId);
            }
        }, function(error) {
            if ( (typeof error.data.deleted !== 'undefined') && (!error.data.deleted) ) {
                var errorTitle = 'SignoffPath Deletion Error';
                var errorMsg = 'Could not delete SignOffPath ' + signoffPath.name + ' because the path is in use by a document.';
                $scope.openSignoffPathErrorModal(errorTitle, errorMsg);
            }
            else {
                $scope.error = error;
            }
        });
    };

    $scope.removeStep = function(step, i) {
        $scope.stepsToRemove.push(step);
        $scope.rightPanel.steps.splice(i, 1);
    };

    $scope.cancelChanges = function() {
        var steps = $scope.rightPanel.steps.concat($scope.stepsToRemove);

        steps.sort(function(a, b) {
            if (a.id > b.id) {
                return 1;
            }
            if (a.id < b.id) {
                return -1;
            }
            return 0;
        });

        // Reset path to the way it was
        $scope.rightPanel.steps = steps;
        $scope.stepsToRemove.length = 0;
        $scope.newSteps.length = 0;

        // Reset edit flags
        for (var i = 0; i < $scope.rightPanel.steps.length; i++) {
            delete $scope.rightPanel.steps[i].edit;
        }

    };

    $scope.extractStepIds = function(steps) {
        var stepIds = [];
        steps.forEach(function (value, i) {
            stepIds.push(value.id);
        });

        return stepIds;
    };

    $scope.openSignoffPathErrorModal = function(errorTitle, errorMsg) {
        var modalInstance = $modal.open({
            templateUrl:'views/admin/adminErrorModal.html',
            controller: adminErrorModalCtrl,
            size: 'sm',
            resolve: {
                errorTitle: function() {
                    return errorTitle;
                },
                errorMsg: function() {
                    return errorMsg;
                }
            }
        })
    };

    /* --------------------- Helpers ---------------------- */

    $scope.removeSignoffPathFromList = function(signoffPathId) {
        var paths = $scope.paths;
        for (var i = 0; i < paths.length; i++) {
            if (paths[i].key.pathId === signoffPathId) {
                $scope.paths.splice(i, 1);
                break;
            }
        }
    };

    $scope.getOrgNameById = function(organizationId) {

        var orgs = $scope.organizations;
        for (var i = 0; i < orgs.length; i++) {
            if (orgs[i].organizationId == organizationId) {
                return orgs[i].name;
            }
        }
        return '';
    };

}

function pendingApprovalsCtrl($scope,
                              $rootScope,
                              manageUsersService,
                              adminDocumentService,
                              adminApprovalService,
                              adminSignoffPathsService,
                              signoffPathsService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    $scope.inProgressDocuments = [];
    $scope.prevDocIdStepsLookup = -1;
    $scope.rightPanel = {
        document: {},
        steps: [],
        tempSteps: []
    };
    $scope.showRightPanel = false;
    $scope.newSteps = [];

    manageUsersService.user.queryAll(function(users) {
        $scope.users = users;
    }, function(error) {
        $scope.err = error;
    });

    adminDocumentService.document.queryByState({state: 'Changing'}, function(inProgressDocs) {
        $scope.inProgressDocuments = inProgressDocs;
    }, function(error) {
        $scope.error = error;
    });


    $scope.changeRightPanel = function(document) {

        // Prevent lookup of steps if already loaded previously
        if ($scope.prevDocIdStepsLookup !== document.id) {

            $scope.showRightPanel = false;
            $scope.rightPanel.document = document;
            $scope.rightPanel.steps.length = 0;

            signoffPathsService.steps.query({documentId: document.id}, function(steps) {
                $scope.rightPanel.steps = steps;
                $scope.prevDocIdStepsLookup = document.id;
                $scope.showRightPanel = true;

            }, function(error) {
                $scope.err = error;
            });

        }

    };

    $scope.filterApprovedSteps = function(approvedStepIds, steps) {
        for (var i = 0; i < steps.length; i++) {
            for (var j = 0; j < approvedStepIds.length; j++) {
                if (steps[i].id === approvedStepIds[j]) {
                    steps[i].approved = true;
                }
            }
        }
        return steps;
    };

    $scope.markStepApproved = function(stepId) {
        var steps = $scope.rightPanel.steps;
        for (var i = 0; i < steps.length; i++) {
            if (steps[i].id === stepId) {
                $scope.rightPanel.steps[i].approved = true;

                // Mark other steps in this group as approved
                // OR steps can only come after
                if ( (steps[i].action === 'THEN') || (steps[i].action === 'START') ) {
                    for (var j = i+1; j < steps.length; j++) {
                        if (steps[j].action === 'OR') {
                            $scope.rightPanel.steps[j].approved = true;
                        }
                        else {
                            break;
                        }
                    }
                }

                // OR steps are before and after
                if (steps[i].action === 'OR') {
                    // Do steps after
                    for (var k = i+1; k < steps.length; k++) {
                        if (steps[k].action === 'OR') {
                            $scope.rightPanel.steps[k].approved = true;
                        }
                        else {
                            break;
                        }
                    }

                    // Do steps before
                    for (var l = i-1; l > -1; l--) {
                        if (steps[l].action === 'OR') {
                            $scope.rightPanel.steps[l].approved = true;
                        }
                        else if ( (steps[l].action === 'THEN') || (steps[l].action === 'START') ) {
                            $scope.rightPanel.steps[l].approved = true;
                            break;
                        }
                        else {
                            break;
                        }
                    }
                }
            }
        }
    };

    // Admin approve step button override function
    $scope.overrideStep = function(step) {
        var documentId = $scope.rightPanel.document.id;
        var stepId = step.id;
        var isTempStep = false;

        // Check if step object has documentId property. Only temp steps have this property
        if (step.hasOwnProperty('documentId')) {
            isTempStep = true
        }

        adminApprovalService.approval.override({documentId: documentId, stepId: stepId}, function(data) {
            $scope.markStepApproved(stepId);
        }, function(error) {
            $scope.error = error;
        })
    };

    $scope.addStep = function() {
        $scope.newSteps.push({
            pathId: $scope.rightPanel.document.signoffPathId,
            action: '',
            user: {}
        });
    };

    $scope.saveNewSteps = function() {

        if ($scope.newSteps.length > 0) {
            adminSignoffPathsService.steps.save({documentId: $scope.rightPanel.document.id}, $scope.newSteps,
                function(data, status, headers, config) {
                    $scope.rightPanel.steps = $scope.rightPanel.steps.concat(data);
                    $scope.newSteps.length = 0;                 // Clear array
                }, function(data, status, headers, config) {
                    $scope.error = status;
                });
        }
    };

    $scope.discardNewSteps = function() {
        $scope.newSteps.length = 0;
    };

}

function moduleSettingsCtrl($scope, $rootScope, $window, adminModuleSettingsService, generalSettingsService) {

    if (!$rootScope.authenticated) {
        $window.location.href = '/';
    }

    // ------------------ Initialize -------------------- //

    generalSettingsService.setting.get({setting: 'redline'}, function(data) {
        $scope.redline = data.value;
    });

    generalSettingsService.setting.get({setting: 'signoff'}, function(data) {
        $scope.signoff = data.value;
    });

    // ---------------- Methods ----------------- //

    // Watch for redline settings change
    $scope.$watch('redline', function(newVal, oldVal) {
        if (typeof oldVal !== 'undefined') {
            if (newVal != oldVal) {

                var payload = {
                    key: {
                        setting: 'redline'
                    },
                    value: $scope.redline
                };

                adminModuleSettingsService.setting.save(payload, function(data, status, headers, config) {

                }, function(data, status, headers, config) {
                    $scope.err = status;
                });
            }
        }
    });

    // Watch signoff paths setting change
    $scope.$watch('signoff', function(newVal, oldVal) {
        if (typeof oldVal !== 'undefined') {
            if (newVal != oldVal) {

                var payload = {
                    key: {
                        setting: 'signoff'
                    },
                    value: $scope.signoff
                };

                adminModuleSettingsService.setting.save(payload, function(data, status, headers, config) {

                }, function(data, status, headers, config) {
                    $scope.err = status;
                });
            }
        }
    });

}

function adminErrorModalCtrl ($scope, $modalInstance, errorTitle, errorMsg) {
    $scope.errorTitle = errorTitle;
    $scope.errorMsg = errorMsg;

    $scope.ok = function() {
        $modalInstance.close();
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };
}


angular
    .module('provesoft')
    .controller('manageUsersCtrl', manageUsersCtrl)
    .controller('documentTypeSetupCtrl', documentTypeSetupCtrl)
    .controller('adminErrorModalCtrl', adminErrorModalCtrl)
    .controller('signoffPathsSetupCtrl', signoffPathsSetupCtrl)
    .controller('pendingApprovalsCtrl', pendingApprovalsCtrl)
    .controller('moduleSettingsCtrl', moduleSettingsCtrl);