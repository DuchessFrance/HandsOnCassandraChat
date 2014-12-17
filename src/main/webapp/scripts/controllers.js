/**
 * Navigation Bar Controller
 */
killrChat.controller('NavBarCtrl', function($rootScope, $scope, $location, User){
    delete $rootScope.generalError;
    $rootScope
        .$on("$routeChangeError",function (event, current, previous, rejection) {
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

/**
 * Sign In Controller
 */
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
                $scope.loginError = httpResponse.data.message;
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
 * Chat Navigation Controller
 */
killrChat.controller('ChatNavigationCtrl',function ($rootScope, $scope, eventBus, User, ChatService, Room) {

    $scope.section = 'home';
    $scope.userRooms = [];
    $scope.userRoomNames = [];

    $scope.loadUsersRooms = function() {
        var userRooms = User.listRooms({fetchSize:100},
            function(){
                $scope.userRooms = userRooms;
                $scope.computeRoomNames();
            },
            function(httpResponse){
                $rootScope.generalError = httpResponse.message;
            });
    };

    $scope.computeRoomNames = function(){
        $scope.userRoomNames = $scope.userRooms.map(function(room) {
            return room.roomName;
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

    $scope.home = function() {
        $scope.section = 'home';
    };

    $scope.allRooms = function() {
        $scope.section = 'allRooms';
    };

    $scope.newRoom = function() {
        $scope.section = 'newRoom';
    };

    $scope.quitRoom = function(roomToLeave) {
        var resource = new Room({room:roomToLeave, participant:$scope.user});
        resource.$removeParticipant()
            .then(function(){
                ChatService.removeParticipant($scope.user, [], $scope.userRooms, roomToLeave);
                $scope.computeRoomNames();
                $scope.section='home';
            })
            .catch(function(httpResponse){
                $rootScope.generalError = httpResponse.message;
            });
    };

    $scope.$evalAsync($scope.loadUsersRooms());
});

/**
 * The real Chat
 */
killrChat.controller('ChatCtrl', function($rootScope, $scope, eventBus, Message, ChatService){

    $scope.messages = [];
    $scope.newMessage = {
        author: $scope.user,
        content:null
    };
    $scope.socket = {
        client: null,
        stomp: null
    }


    $scope.loadInitialRoomMessages = function() {
        var messages = Message.load({roomName:$scope.currentRoom.roomName, fetchSize: 20},
        function() {
            $scope.messages = messages;
            $scope.initSockets();
        },
        function (httpResponse) {
            $rootScope.generalError = httpResponse.data;
        });
    };


    $scope.postMessage = function(){
        if($scope.newMessage.content){
            var message = new Message($scope.newMessage);
            message.$create({roomName:$scope.currentRoom.roomName})
                .then(function(){
                    delete $scope.newMessage.content;
                })
                .catch(function(httpResponse) {
                    $rootScope.generalError = httpResponse.data;
                });
        } else {
            $rootScope.generalError = 'Hey dude, post a non blank message ...';
        }
    };
    $scope.$evalAsync($scope.loadInitialRoomMessages());

    $scope.notifyNewMessage = function(message) {
        $scope.$apply(function(){
            $scope.messages.push(angular.fromJson(message.body));
        });
    };

    $scope.notifyParticipant = function(message) {
        var participant = angular.fromJson(message.body);
        var status = message.headers.status;
        $scope.$apply(function(){
              if(status == 'JOIN') {
                  $scope.currentRoom.participants.push(participant);
              } else if(status == 'LEAVE') {
                  ChatService.removeMatchingParticipant($scope.currentRoom.participants, participant);
              }
        });
    };

    $scope.initSockets = function() {
        $scope.socket.client = new SockJS('/chat');
        $scope.socket.stomp = Stomp.over($scope.socket.client);
        $scope.socket.stomp.connect({}, function() {
            $scope.socket.stomp.subscribe('/topic/messages/'+$scope.currentRoom.roomName, $scope.notifyNewMessage);
            $scope.socket.stomp.subscribe('/topic/participants/'+$scope.currentRoom.roomName, $scope.notifyParticipant);
        });
    };


    $scope.closeSocket = function() {
        if($scope.socket.client) {
            $scope.socket.client.close();
        }
        if($scope.socket.stomp) {
            $scope.socket.stomp.disconnect();
        }
    };

    eventBus.onMsg('switchRoom', $scope, function() {
        $scope.closeSocket();
        $scope.loadInitialRoomMessages();
    });

});

/**
 * Rooms Management
 */
killrChat.controller('RoomsListCtrl', function($rootScope, $scope, ChatService, Room){

    $scope.allRooms = [];

    $scope.loadInitialRooms = function() {
        var rooms = Room.list({fetchSize:10},
        function(){
            $scope.allRooms = rooms;
        },
        function(httpResponse){
           $rootScope.generalError = httpResponse.message;
        });
    };

    $scope.joinRoom = function(roomToJoin) {
        var resource = new Room({room:roomToJoin, participant:$scope.user});
        resource.$addParticipant()
        .then(function(){

            ChatService.addParticipant($scope.user, $scope.allRooms, $scope.userRooms, roomToJoin)
            $scope.computeRoomNames();
        })
        .catch(function(httpResponse){
            $rootScope.generalError = httpResponse.message;
        });
    };

    $scope.quitRoom = function(roomToLeave) {
        var resource = new Room({room:roomToLeave, participant:$scope.user});
        resource.$removeParticipant()
            .then(function(){
                ChatService.removeParticipant($scope.user, $scope.allRooms, $scope.userRooms, roomToLeave)
                $scope.computeRoomNames();
            })
            .catch(function(httpResponse){
                $rootScope.generalError = httpResponse.message;
            });
    };
    $scope.$evalAsync($scope.loadInitialRooms());
});

/**
 * Room Creation
 */
killrChat.controller('newRoomCtrl', function($rootScope, $scope, Room){
    $scope.input_ok = 'form-group has-feedback';
    $scope.input_error = 'form-group has-feedback has-error';
    $scope.room_form_error= null;
    $scope.newRoom = {
        creator: $scope.user,
        roomName:null
    };

    $scope.createNewRoom = function() {
        new Room($scope.newRoom)
            .$create()
            .then(function(){
                var copy = angular.copy($scope.newRoom);
                $scope.userRooms.push(copy);
                $scope.computeRoomNames();
                delete $scope.newRoom.roomName;
            })
            .catch(function(httpResponse){
               $scope.room_form_error = httpResponse.data;
            });
    };

});
