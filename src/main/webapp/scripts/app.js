'use strict';

var killrChat = angular.module('KillrChat', ['ngRoute','ngResource','ui.bootstrap','angularSpinner'])
    .config(function ($routeProvider, $httpProvider) {
        $httpProvider.defaults.headers.common = {'Accept': 'application/json', 'Content-Type': 'application/json'};

        $routeProvider
            .when('/', {
                templateUrl: 'views/login.html'
            })
            .when('/chat', {
            	templateUrl: 'views/chat.html'
            })
            .otherwise({
                redirectTo: '/'
            });

});