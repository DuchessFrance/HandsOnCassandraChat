/**
 * Navigation Bar Controller
 */
killrChat.controller('NavBarCtrl', function($rootScope, $scope, $location, User, Security){
    delete $rootScope.generalError;

    $rootScope.$on("$routeChangeStart", function(event, next) {
        delete $rootScope.generalError;
        if(!$rootScope.user && next !='/'){
            Security.fetchAuthenticatedUser()
                .$promise
                .then(function(user){
                    $rootScope.user = user;
                })
                .catch(function(){
                    $location.path('/');
                });
        }
    });

    $rootScope.$on("$routeChangeError",function (event, current, previous, rejection) {
        console.info("route change error "+rejection.data.message);
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
killrChat.controller('ChatNavigationCtrl',function ($scope, Room, ChatService) {

    $scope.section = 'home';
    $scope.state = {
        currentRoom: {}
    };

    //$scope.currentRoom = {};

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
                $scope.state.currentRoom = currentRoom;
                $scope.section = 'room';
                $scope.$broadcast('loadRoomData');
            })
            .catch($scope.displayGeneralError);
    };

    $scope.quitRoomBackHome = function(roomToLeave) {
        $scope.section = 'home';
        ChatService.removeRoomFromUserRoomsList($scope.user.chatRooms, roomToLeave);
        $scope.state.currentRoom = {};
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
killrChat.controller('ChatScrollCtrl', function($scope, Message, ChatService){
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

    // Angular trick because errorDisplay() return the reference of the original function
    $scope.displayGeneralError = function(message) {
        $scope.errorDisplay()(message);
    };

    $scope.loadInitialRoomMessages = function() {
        $scope.messages = Message.load({roomName:$scope.state.currentRoom.roomName, fetchSize: 20})
        $scope.messages.$promise.then(function(){
            self.initSockets();
        })
        .catch($scope.displayGeneralError);
    };

    $scope.loadPreviousMessages = function() {
        var promise = Message.load({roomName:$scope.state.currentRoom.roomName, fromMessageId: $scope.messages[0].messageId, fetchSize: 20}).$promise;
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
            new Message($scope.newMessage)
                .$create({roomName:$scope.state.currentRoom.roomName})
                .then(function(){
                    delete $scope.newMessage.content;
                })
                .catch($scope.displayGeneralError);
        } else {
            $scope.displayGeneralError('Hey dude, post a non blank message ...');
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
                $scope.state.currentRoom.participants.push(participant);
            } else if(status == 'LEAVE') {
                ChatService.removeMatchingParticipant($scope.state.currentRoom.participants, participant);
            }
        });
    };

    this.initSockets = function() {
        $scope.socket.client = new SockJS('/chat');
        $scope.socket.stomp = Stomp.over($scope.socket.client);
        $scope.socket.stomp.debug = function(str) {};
        $scope.socket.stomp.connect({}, function() {
            $scope.socket.stomp.subscribe('/topic/messages/'+$scope.state.currentRoom.roomName, self.notifyNewMessage);
            $scope.socket.stomp.subscribe('/topic/participants/'+$scope.state.currentRoom.roomName, self.notifyParticipant);
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

    $scope.$eval($scope.loadInitialRoomMessages());
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
