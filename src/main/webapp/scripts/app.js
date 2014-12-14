'use strict';

var killrChat = angular.module('KillrChat', ['ngRoute','ngResource','ui.bootstrap','validation.match'])
    .config(function ($routeProvider, $httpProvider) {
        $httpProvider.defaults.headers.common = {'Accept': 'application/json', 'Content-Type': 'application/json'};

        $routeProvider
            .when('/', {
                templateUrl: 'views/login.html'
            });
//            .when('/user/:action/:param1/:param2?', {
//            	templateUrl: 'assets/views/user.html'
//            })
//	        .when('/tweet/:action/:param1/:param2?', {
//	        	templateUrl: 'assets/views/tweet.html'
//	        })
//	        .when('/line/:type/:param1', {
//	        	templateUrl: 'assets/views/line.html'
//	        });
        
});




