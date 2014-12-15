'use strict';

var killrChat = angular.module('KillrChat', ['ngRoute','ngResource','ui.bootstrap','validation.match'])
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
//	        .when('/tweet/:action/:param1/:param2?', {
//	        	templateUrl: 'assets/views/tweet.html'
//	        })
//	        .when('/line/:type/:param1', {
//	        	templateUrl: 'assets/views/line.html'
//	        });

});

killrChat.factory('eventBus', ['$rootScope', function($rootScope) {
    var msgBus = {};
    msgBus.emitMsg = function(msg) {
        $rootScope.$emit(msg);
    };
    msgBus.onMsg = function(msg, scope, func) {
        var unbind = $rootScope.$on(msg, func);
        scope.$on('$destroy', unbind);
    };
    return msgBus;
}]);



