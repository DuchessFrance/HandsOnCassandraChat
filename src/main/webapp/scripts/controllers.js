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


    $rootScope.displayGeneralError = function (httpResponse) {
        if(httpResponse) {
            if(httpResponse.data) {
                if(httpResponse.data.message) {
                    $rootScope.generalError = httpResponse.data.message;
                } else {
                    $rootScope.generalError = httpResponse.data;
                }
            } else if(httpResponse.message) {
                $rootScope.generalError = httpResponse.message;
            } else {
                $rootScope.generalError = httpResponse;
            }
        }
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

        new User().$login({
            j_username: $scope.username,
            j_password: $scope.password,
            _spring_security_remember_me: $scope.rememberMe
        })
        .then(function() {
            // Reset any previous error
            delete $scope.loginError;

            $rootScope.user = User.load({login: $scope.username});

            $rootScope.user.$promise
            .then(function(){
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
killrChat.controller('ChatNavigationCtrl',function ($scope, User, Room, ChatService) {

    $scope.section = 'home';
    $scope.currentRoom = {};

    $scope.home = function() {
        $scope.section = 'home';
    };

    $scope.allRooms = function() {
        $scope.section = 'allRooms';
    };

    $scope.newRoom = function() {
        $scope.section = 'newRoom';
    };

    $scope.enterRoom = function(roomToEnter) {
        Room.load({roomName:roomToEnter}).$promise
            .then(function(currentRoom){
                $scope.currentRoom = currentRoom;
                $scope.section = 'room';
                $scope.$broadcast('loadRoomData');
            })
            .catch($scope.displayGeneralError);
    };

    $scope.quitRoomBackHome = function(roomToLeave) {
        $scope.section = 'home';
        ChatService.removeRoomFromUserRoomsList($scope.user.chatRooms, roomToLeave);
        $scope.currentRoom = {};
    };
});

/**
 * User Rooms Controller
 */
killrChat.controller('UserRoomsCtrl', function($scope, Room) {

    $scope.quitRoom = function(roomToLeave) {
        var resource = new Room({roomName:roomToLeave, participant:$scope.user});
        resource.$removeParticipant()
        .then(function(){
            $scope.quitRoomBackHome(roomToLeave);
        })
        .catch($scope.displayGeneralError);
    };
});

/**
 * The real Chat
 */
killrChat.controller('ChatCtrl', function($rootScope, $scope, Room, Message, ChatService){

    var self = this;
    $scope.messages = [];
    $scope.newMessage = {
        author: $scope.user,
        content:null
    };
    $scope.socket = {
        client: null,
        stomp: null
    };

    this.loadInitialRoomMessages = function() {
        $scope.messages = Message.load({roomName:$scope.currentRoom.roomName, fetchSize: 20})
        $scope.messages.$promise.then(function(){
            self.initSockets();
            $scope.$broadcast('resetScrollState');
        })
        .catch($scope.displayGeneralError);
    };

    $scope.loadPreviousMessages = function() {
        var promise = Message.load({roomName:$scope.currentRoom.roomName, fromMessageId: $scope.messages[0].messageId, fetchSize: 20}).$promise;

        promise.then(function(messages) {
            messages.reverse().forEach(function(message){
                $scope.messages.unshift(message);
            });
        })
        .catch($scope.displayGeneralError);

        return promise;
    };

    $scope.postMessage = function(){
        if($scope.newMessage.content){
            var message = new Message($scope.newMessage);
            message.$create({roomName:$scope.currentRoom.roomName})
            .then(function(){
                delete $scope.newMessage.content;
            })
            .catch($scope.displayGeneralError);
        } else {
            $rootScope.generalError = 'Hey dude, post a non blank message ...';
        }
    };

    this.notifyNewMessage = function(message) {
        $scope.$apply(function(){
            $scope.messages.push(angular.fromJson(message.body));
        });
    };

    this.notifyParticipant = function(message) {
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

    this.initSockets = function() {
        $scope.socket.client = new SockJS('/chat');
        $scope.socket.stomp = Stomp.over($scope.socket.client);
        $scope.socket.stomp.debug = function(str) {};
        $scope.socket.stomp.connect({}, function() {
            $scope.socket.stomp.subscribe('/topic/messages/'+$scope.currentRoom.roomName, self.notifyNewMessage);
            $scope.socket.stomp.subscribe('/topic/participants/'+$scope.currentRoom.roomName, self.notifyParticipant);
        });
    };


    this.closeSocket = function() {
        if($scope.socket.client) {
            $scope.socket.client.close();
        }
        if($scope.socket.stomp) {
            $scope.socket.stomp.disconnect();
        }
    };

    $scope.$on('loadRoomData', function() {
        self.closeSocket();
        self.loadInitialRoomMessages();
    });

    $scope.$eval(self.loadInitialRoomMessages());

});

/**
 * Rooms Management
 */
killrChat.controller('RoomsListCtrl', function($scope, ChatService, Room){

    var self = this;
    $scope.allRooms = [];

    this.loadInitialRooms = function() {
        $scope.allRooms = Room.list({fetchSize:100});
        $scope.allRooms.$promise.catch($scope.displayGeneralError);
    };

    $scope.joinRoom = function(roomToJoin) {
        new Room({roomName:roomToJoin.roomName, participant:$scope.user})
            .$addParticipant()
            .then(function(){
                ChatService.addParticipantToRoom($scope.user, $scope.allRooms, roomToJoin);
                ChatService.addRoomToUserRoomsList($scope.user.chatRooms, roomToJoin.roomName);
            })
            .catch($scope.displayGeneralError);
    };

    $scope.quitRoom = function(roomToLeave) {
        new Room({roomName:roomToLeave.roomName, participant:$scope.user})
            .$removeParticipant()
            .then(function(){
                ChatService.removeParticipantFromRoom($scope.user, $scope.allRooms, roomToLeave);
                ChatService.removeRoomFromUserRoomsList($scope.user.chatRooms, roomToLeave.roomName);
                //$scope.$emit('quitRoom',roomToLeave.roomName);
            })
            .catch($scope.displayGeneralError);
    };
    $scope.$evalAsync(self.loadInitialRooms());
});

/**
 * Room Creation
 */
killrChat.controller('NewRoomCtrl', function($scope, Room){
    $scope.room_form_error= null;
    $scope.newRoom = {
        creator: $scope.user,
        roomName:null,
        banner:null
    };

    $scope.createNewRoom = function() {
        new Room($scope.newRoom)
            .$create()
            .then(function(){
                $scope.user.chatRooms.push($scope.newRoom.roomName);
                delete $scope.newRoom.roomName;
                delete $scope.newRoom.banner;
            })
            .catch(function(httpResponse){
               $scope.room_form_error = httpResponse.data;
            });
    };

});
