/**
 * Login page
 */

killrChat.controller('NavBarCtrl', function($rootScope, $scope, $location, User){
    delete $rootScope.generalError;
    $rootScope
        .$on("$routeChangeError",function (event, current, previous, rejection) {
            console.error("root change error");
            $rootScope.generalError = rejection.data.message;
        });

    $scope.closeAlert = function() {
        delete $rootScope.generalError;
    };

    $scope.logout = function() {
        User.logout(function() {
            $location.path('/');
            delete $rootScope.user;
        });
    };
});

killrChat.controller('SignInCtrl', function ($rootScope, $scope, $modal, $location, User) {

    $scope.username = null;
    $scope.password = null;
    $scope.rememberMe = false;

    $scope.open = function () {
        var modalInstance = $modal.open({
            templateUrl: 'signUpModal.html',
            controller: 'SignUpModalCtrl'
        });

        //createdUser is an instance of the resource User
        modalInstance.result.then(function (createdUser) {
            $scope.username = createdUser.login;
            $scope.password = createdUser.password;
            $scope.userCreated = true;
        });
    };

    $scope.login = function() {

        var user = new User();

        //call $login method on the resource User
        user.$login({
            j_username: $scope.username,
            j_password: $scope.password,
            _spring_security_remember_me: $scope.rememberMe
        })
        .then(function() {
            // Reset any previous error
            delete $scope.loginError;

            user.$load({login: $scope.username})
                .then(function(){
                    $rootScope.user = user;

                    //Switch to chat view
                    $location.path('/chat');
                })
                .catch(function(httpResponse){
                    $scope.loginError = httpResponse.message;
                });
        })
        .catch(function(httpResponse){
                $scope.loginError = httpResponse;
        });
    };
});

/**
 * Signup Modal Panel
 */
killrChat.controller('SignUpModalCtrl',function ($scope, $modalInstance, User) {

    $scope.input_ok = 'form-group has-feedback';
    $scope.input_error = 'form-group has-feedback has-error';

    $scope.user = new User({
        login:null,
        password:null,
        passwordConfirm:null,
        firstname:null,
        lastname:null,
        nickname:null,
        bio:null,
        email:null
    });

    $scope.ok = function () {
        $scope.user.$create()
        .then(function(){
            delete $scope.user.passwordConfirm;
            $modalInstance.close($scope.user);
        })
        .catch(function(error) {
            $scope.user_create_error = error.data;
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss();
    };
});

/**
 * Rooms management
 */

killrChat.controller('ChatRoomCtrl',function ($rootScope, $scope, eventBus, User, Room) {

    $scope.section = 'home';
    $scope.userRooms = [];

    $scope.loadUsersRooms = function() {
        var userRooms = User.listRooms({fetchSize:100},
            function(){
                $scope.userRooms = userRooms;
            },
            function(httpResponse){
                $rootScope.generalError = httpResponse.message;
            });
    };

    $scope.switchRoom = function(roomName) {
        var targetRoom = Room.load({roomName:roomName},
            function(){
                $scope.currentRoom = targetRoom;
                $scope.section = 'room';
                eventBus.emitMsg('switchRoom');
            },
            function(httpResponse){
                $rootScope.generalError = httpResponse.message;
            });
    };

    $scope.leaveRoom = function(roomToLeave) {

        Room.$removeParticipant({room:roomToLeave })
            .then(function(httpResponse){})
            .catch(function(httpResponse){});

    };

    $scope.$evalAsync($scope.loadUsersRooms());
});

/**
 * The real Chat
 */
killrChat.controller('ChatCtrl', function($rootScope, $scope, eventBus, Message){

    $scope.messages = [];
    $scope.participants = [];


    $scope.loadInitialRoomMessages = function() {
        var messages = Message.load({roomName:$scope.currentRoom.roomName, fetchSize: 20},
        function() {
            $scope.messages = messages;
        },
        function (httpResponse) {
            $rootScope.generalError = httpResponse.data;
        });
    };

    $scope.postMessage = function(){
        if($scope.message){
            var message = new Message({author: $scope.user, content: $scope.message});
            message.$create({roomName:$scope.currentRoom.roomName})
                .then(function(){
                    $scope.loadInitialRoomMessages();
                    delete $scope.message;
                })
                .catch(function(httpResponse) {
                    $rootScope.generalError = httpResponse.data;
                });
        } else {
            $rootScope.generalError = 'Hey dude, post a non blank message ...';
        }
    };

    $scope.$evalAsync($scope.loadInitialRoomMessages());

    eventBus.onMsg('switchRoom', $scope, function() {
        $scope.loadInitialRoomMessages();
    });

});
