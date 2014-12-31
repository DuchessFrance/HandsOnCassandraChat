/**
 * SignUp User Form
 */
killrChat.directive('validFor', function() {
    return {
        priority: 1,
        restrict: 'A',
        scope: {
            validFor: '=' // bind value to be validated
        },
        link: function (scope, el) {
            scope.$watch(function() { // watcher does not return anything, just invoked
                el.toggleClass('has-error', !scope.validFor);
            });
        }
    }
});

killrChat.directive('passwordMatch', function() {
    return {
        priority: 1,
        restrict: 'A',
        require: 'ngModel',
        scope: {
          passwordMatch: '='
        },
        link: function (scope, el, attrs, ngCtrl) {

            scope.$watch(function(){
                // check for equality in the watcher and return validity flag
                var modelValue = ngCtrl.$modelValue;
                return (ngCtrl.$pristine && angular.isUndefined(modelValue)) || angular.equals(modelValue, scope.passwordMatch);
            },function(validBoolean){
                // set validation with validity in the listener
                ngCtrl.$setValidity('passwordMatch', validBoolean);
            });
        }
    }
});

/**
 * Chat New Message
 */
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

/**
 * Chat Messages Zone
 */
killrChat.directive('chatZone', function($log, usSpinnerService) {
    return {
        priority: 1,
        restrict: 'E',
        replace: true,
        templateUrl: '/views/templates/chatWindow.html',
        scope: {
            state: '=',
            user: '=',
            errorDisplay: '&'
        },
        controller: 'ChatScrollCtrl',
        link: function (scope, root) {
            var scrollMode = 'display';
            var loadMoreData = true;
            var element = root[0].querySelector('#chat-scroll');
            if(!element) {
                scope.displayGeneralError("Cannot find element with id 'chat-scroll' in the template of 'chatZone' directive");
                return;
            }

            var wrappedElement = angular.element(element);
            element.scrollTop = 10;

            scope.$watch(function(){ // watch on currentRoom
                return scope.state.currentRoom;
            },
            function(){ // on change of room reset scroll state
                loadMoreData = true;
                scrollMode = 'display';
                scope.closeSocket();
                scope.loadInitialRoomMessages();
            });

            //Change in the list of chat messages should be intercepted
            scope.$watchCollection(
                function() {  // watch on chat message
                    return scope.messages;
                },
                function(newMessages,oldMessages) { // on change of chat messages
                    if(scrollMode === 'display') {
                        if(newMessages.length > oldMessages.length){
                            element.scrollTop = element.scrollHeight;
                        }
                    } else if(scrollMode === 'loading') {
                        element.scrollTop = 10;
                    }
                }
            );

            wrappedElement.bind('scroll', function() {
                if(element.clientHeight + element.scrollTop + 1 >= element.scrollHeight) {
                    scrollMode = 'display';
                } else if(element.scrollTop == 0 && loadMoreData) {
                    scope.$apply(function(){
                        usSpinnerService.spin('loading-spinner');
                        scrollMode = 'loading';
                        scope.loadPreviousMessages()
                        .then(function(messages){
                            // if no more message found, stop loading messages on next calls
                            if(messages.length == 0) {
                                loadMoreData = false;
                            }
                            usSpinnerService.stop('loading-spinner');
                        });
                    });

                } else {
                    scrollMode = 'fixed';
                }
            });

            wrappedElement.on('$destroy', function() {
                wrappedElement.unbind('scroll');
            });
        }
    }
});
