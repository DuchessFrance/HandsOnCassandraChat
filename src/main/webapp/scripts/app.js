'use strict';

var killrChat = angular.module('KillrChat', ['ngRoute','ngResource','ui.bootstrap','angularSpinner'])
    .config(function ($routeProvider, $httpProvider) {
        $httpProvider.defaults.headers.common = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'};

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