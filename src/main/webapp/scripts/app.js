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

killrChat.directive('ngEnter', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if(event.which === 13) {
                scope.$apply(function(){
                    scope.$eval(attrs.ngEnter, {'event': event});
                });

                event.preventDefault();
            }
        });
    };
});

killrChat.filter('onlyMinute', function(){
    return function(dateString) {
        var regexp = /[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:([0-9]{2}:[0-9]{2})/;
        return dateString.replace(regexp,"$1");
    }
});

killrChat.filter('displayUserName', function(){
    return function(user) {
        return user.firstname+' '+user.lastname;
    }
});
