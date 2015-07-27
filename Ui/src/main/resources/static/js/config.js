'use strict';

function config($stateProvider, $locationProvider, $urlRouterProvider, $ocLazyLoadProvider) {
    $urlRouterProvider.otherwise("/news-feed");

    $ocLazyLoadProvider.config({
        // Set to true if you want to see what and when is dynamically loaded
        debug: false
    });

    $stateProvider

        .state('home', {
            abstract: true,
            url: "/",
            templateUrl: "views/common/content.html"
        })
        .state('home.main', {
            url: "main",
            templateUrl: "views/main.html",
            data: { pageTitle: 'Example view' }
        })
        .state('home.news-feed', {
            url: "news-feed",
            templateUrl: "views/news_feed.html",
            data: { pageTitle: 'News Feed' }
        })

        .state('process-viewer', {
            abstract: true,
            url: "/process-viewer",
            templateUrl: "views/common/content.html"
        })
        .state('process-viewer.document-lookup', {
            url: "/document-lookup",
            templateUrl: "views/process-viewer/document_lookup.html",
            controller: 'documentLookupCtrl',
            data: { pageTitle: 'Document Lookup' }
        })
        .state('process-viewer.document-creation', {
            url: "/document-creation",
            templateUrl: "views/process-viewer/document_creation.html",
            controller: 'documentCreationCtrl',
            data: { pageTitle: 'Document Creation' },
            resolve: {
                loadPlugin: function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            files: [
                                'css/plugins/dropzone/basic.css',
                                'css/plugins/dropzone/dropzone.css',
                                'js/plugins/dropzone/dropzone.js'
                            ]
                        },
                        {
                            name:'ui.select',
                            files: [
                                'css/plugins/ui-select/select.min.css',
                                'css/plugins/ui-select/selectize.min.css',
                                'js/plugins/ui-select/select.min.js'
                            ]
                        }
                    ]);
                }
            }
        })
        .state('process-viewer.document-revision', {
            url: "/document-revision?documentId",
            templateUrl: "views/process-viewer/document_revision.html",
            controller: 'documentRevisionCtrl',
            data: { pageTitle: 'Document Revision' },
            resolve: {
                loadPlugin: function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            files: [
                                'css/plugins/dropzone/basic.css',
                                'css/plugins/dropzone/dropzone.css',
                                'js/plugins/dropzone/dropzone.js'
                            ]
                        }
                    ])
                }
            }
        })

        .state('admin', {
            abstract: true,
            url: "/admin",
            templateUrl: "views/common/content.html"
        })
        .state('admin.manageUsers', {
            url: "/manage-users",
            templateUrl: "views/admin/manage_users.html",
            controller: manageUsersCtrl,
            data: {
                pageTitle: 'Admin | User Management'
            },
            resolve: {
                loadPlugin: function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            files: ['css/plugins/iCheck/custom.css','js/plugins/iCheck/icheck.min.js']
                        },
                        {
                            name:'ui.select',
                            files: [
                                'css/plugins/ui-select/select.min.css',
                                'css/plugins/ui-select/select2.min.css',
                                'css/plugins/ui-select/selectize.min.css',
                                'js/plugins/ui-select/select.min.js'
                            ]
                        }
                    ]);
                }
            }
        })
        .state('admin.documentTypeSetup', {
            url: "/document-type-setup",
            templateUrl: "views/admin/document_type_setup.html",
            controller: documentTypeSetupCtrl,
            data: {
                pageTitle: 'Admin | Document Setup'
            },
            resolve: {
                loadPlugin: function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                        },
                        {
                        }
                    ]);
                }
            }
        })
        .state('admin.signoffPathsSetup', {
            url: "/signoff-paths-setup",
            templateUrl: "views/admin/signoff_paths_setup.html",
            controller: signoffPathsSetupCtrl,
            data: {
                pageTitle: 'Admin | Signoff Path Setup'
            },
            resolve: {
                loadPlugin: function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            files: ['css/plugins/iCheck/custom.css','js/plugins/iCheck/icheck.min.js']
                        },
                        {
                            name:'ui.select',
                            files: [
                                'css/plugins/ui-select/select.min.css',
                                'css/plugins/ui-select/select2.min.css',
                                'css/plugins/ui-select/selectize.min.css',
                                'js/plugins/ui-select/select.min.js'
                            ]
                        }
                    ]);
                }
            }
        })
        .state('admin.moduleSettings', {
            url: "/module-settings",
            templateUrl: "views/admin/module_settings.html",
            controller: moduleSettingsCtrl,
            data: { pageTitle: 'Admin | Settings' }
        })

        .state('home.profile', {
            url: "profile",
            templateUrl: "views/profile.html",
            data: { pageTitle: 'My Profile' }
        });


    $locationProvider.html5Mode(true);

}
angular
    .module('provesoft')
    .config(config)
    .run(function($rootScope, $state) {
        $rootScope.$state = $state;
    });
