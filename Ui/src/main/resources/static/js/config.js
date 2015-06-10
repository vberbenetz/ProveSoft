/**
 * INSPINIA - Responsive Admin Theme
 * Copyright 2015 Webapplayers.com
 *
 * Inspinia theme use AngularUI Router to manage routing and views
 * Each view are defined as state.
 * Initial there are written state for all view in theme.
 *
 */
function config($stateProvider, $locationProvider, $urlRouterProvider, $ocLazyLoadProvider) {
    $urlRouterProvider.otherwise("/main");

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
        .state('admin', {
            abstract: true,
            url: "/admin",
            templateUrl: "views/common/content.html"
        })
        .state('admin.manageUsers', {
            url: "/manage-users",
            templateUrl: "views/manage_users.html",
            data: { pageTitle: 'Admin | User Management' }
        })
        .state('home.profile', {
            url: "profile",
            templateUrl: "views/profile.html",
            data: { pageTitle: 'My Profile' }
        });


    $locationProvider.html5Mode(true);

}
angular
    .module('inspinia')
    .config(config)
    .run(function($rootScope, $state) {
        $rootScope.$state = $state;
    });
