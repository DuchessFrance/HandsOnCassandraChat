killrChat.service('RememberMeService',function($rootScope, $location, RememberMe){

    this.fetchAuthenticatedUser = function(nextRoute){
        if(!$rootScope.user && nextRoute !='/'){
            RememberMe.fetchAuthenticatedUser()
            .$promise
            .then(function(user){
                $rootScope.user = user;
                $rootScope.user.chatRooms.sort();
            })
            .catch(function(){
                $location.path('/');
            });
        }
    };
});

killrChat.service('GeneralErrorService', function($rootScope){
    this.displayGeneralError = function (httpResponse) {
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

    this.clearGeneralError = function() {
        delete $rootScope.generalError;
    };
});

killrChat.service('SecurityService', function($rootScope, $location, User) {

    this.login = function($scope) {
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
                        $rootScope.user.chatRooms.sort();
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
    }

    this.logout = function() {
       User.logout(function() {
           $location.path('/');
           delete $rootScope.user;
       });
    };
});

killrChat.service('UserRoomsService', function(){

    this.addRoomToUserRoomsList = function(userRooms, roomToJoin) {
        var indexOf = userRooms.indexOf(roomToJoin);
        if(indexOf == -1){
            userRooms.push(roomToJoin);
            userRooms.sort();
        }
    };

    this.removeRoomFromUserRoomsList = function(userRooms, roomToLeave) {
        var indexOf = userRooms.indexOf(roomToLeave);
        if(indexOf > -1){
            userRooms.splice(indexOf,1);
        }
    }
});

killrChat.service('ParticipantService', function(){
    var self = this;
    this.sortParticipant = function(participantA,participantB){
        return participantA.firstname.localeCompare(participantB.firstname);
    };

    this.addParticipantToCurrentRoom = function(currentRoom, participantToAdd) {
        currentRoom.participants.push(participantToAdd);
        currentRoom.participants.sort(self.sortParticipant);
    };

    this.removeParticipantFromCurrentRoom = function(currentRoom, participantToRemove) {
        var indexToRemove = currentRoom.participants.map(function(p){return p.login}).indexOf(participantToRemove.login);
        currentRoom.participants.splice(indexToRemove, 1);
    };
});

killrChat.service('NavigationService', function(Room, ParticipantService, UserRoomsService, GeneralErrorService){
    this.enterRoom = function($scope, roomToEnter) {
        Room.load({roomName:roomToEnter}).$promise
            .then(function(currentRoom){
                $scope.state.currentRoom = currentRoom;
                $scope.state.currentRoom.participants.sort(ParticipantService.sortParticipant);
                $scope.section = 'room';
            })
            .catch(GeneralErrorService.displayGeneralError);
    };

    this.quitRoomBackHome = function($scope, roomToLeave, GeneralErrorService) {
        new Room({roomName:roomToLeave, participant:$scope.user})
            .$removeParticipant()
            .then(function(){
                $scope.section = 'home';
                UserRoomsService.removeRoomFromUserRoomsList($scope.user.chatRooms, roomToLeave);
                $scope.state.currentRoom = {};
            })
            .catch(GeneralErrorService.displayGeneralError);
    };
});

killrChat.service('RealChatService', function(Message, ParticipantService, GeneralErrorService){

    var self = this;

    this.notifyNewMessage = function($scope,message) {
        $scope.$apply(function(){
            $scope.messages.push(angular.fromJson(message.body));
        });
    };

    this.notifyParticipant = function($scope,message) {
        var participant = angular.fromJson(message.body);
        var status = message.headers.status;
        $scope.$apply(function(){
            if(status == 'JOIN') {
                ParticipantService.addParticipantToCurrentRoom($scope.state.currentRoom, participant);
            } else if(status == 'LEAVE') {
                ParticipantService.removeParticipantFromCurrentRoom($scope.state.currentRoom, participant);
            }
        });
    };

    this.initSockets = function($scope) {
        self.closeSocket($scope);
        $scope.socket.client = new SockJS('/chat');
        $scope.socket.stomp = Stomp.over($scope.socket.client);
        $scope.socket.stomp.debug = function(str) {};
        $scope.socket.stomp.connect({}, function() {
            $scope.socket.stomp.subscribe('/topic/messages/'+$scope.state.currentRoom.roomName,
                function(message){
                    self.notifyNewMessage($scope,message);
                });
            $scope.socket.stomp.subscribe('/topic/participants/'+$scope.state.currentRoom.roomName,
                function(message) {
                    self.notifyParticipant($scope, message);
                });
        });
    };

    this.loadInitialRoomMessages = function($scope) {
        $scope.messages = Message.load({roomName:$scope.state.currentRoom.roomName, fetchSize: 20});
        $scope.messages.$promise.then(function(){
            self.initSockets($scope);
        })
        .catch(GeneralErrorService.displayGeneralError);
    };

    this.loadPreviousMessages = function($scope) {
        var promise = Message.load({roomName:$scope.state.currentRoom.roomName, fromMessageId: $scope.messages[0].messageId, fetchSize: 20}).$promise;
        promise.then(function(messages) {
            messages.reverse().forEach(function(message){
                $scope.messages.unshift(message);
            });
        }).catch(GeneralErrorService.displayGeneralError);

        return promise;
    };

    this.postMessage = function($scope){
        if($scope.newMessage.content){
            new Message($scope.newMessage)
                .$create({roomName:$scope.state.currentRoom.roomName})
                .then(function(){
                    delete $scope.newMessage.content;
                })
                .catch(GeneralErrorService.displayGeneralError);
        } else {
            GeneralErrorService.displayGeneralError('Hey dude, post a non blank message ...');
        }
    };

    this.closeSocket = function($scope) {
        if($scope.socket.client) {
            $scope.socket.client.close();
        }
        if($scope.socket.stomp) {
            $scope.socket.stomp.disconnect();
        }
    };
});

killrChat.service('RoomService', function(Room, ParticipantService, GeneralErrorService){

    var self = this;
    this.findMatchingRoom = function(rooms,matchingRoom) {
        return rooms.filter(function(room){
            return matchingRoom.roomName == room.roomName;
        })[0];
    };

    this.sortRooms = function(roomA,roomB){
        return roomA.roomName.localeCompare(roomB.roomName);
    };

    this.addMeToThisRoom = function(me, allRoomsList, targetRoom) {
        var roomInExistingList = self.findMatchingRoom(allRoomsList, targetRoom);

        roomInExistingList.participants.push({
            login:me.login,
            firstname:me.firstname,
            lastname:me.lastname
        });
        roomInExistingList.participants.sort(ParticipantService.sortParticipant);
    };

    this.removeMeFromThisRoom = function(participant, allRoomsList, targetRoom) {
        var roomInExistingList = self.findMatchingRoom(allRoomsList, targetRoom);
        if(roomInExistingList) {
            ParticipantService.removeParticipantFromCurrentRoom(roomInExistingList, participant);
        }
    };

    this.addRoomToUserRoomsList = function(userRooms, roomToJoin) {
        var indexOf = userRooms.indexOf(roomToJoin);
        if(indexOf == -1){
            userRooms.push(roomToJoin);
            userRooms.sort();
        }
    };

    this.removeRoomFromUserRoomsList = function(userRooms, roomToLeave) {
        var indexOf = userRooms.indexOf(roomToLeave);
        if(indexOf > -1){
            userRooms.splice(indexOf,1);
        }
    };

    this.loadInitialRooms = function($scope) {
        $scope.allRooms = Room.list({fetchSize:100});
        $scope.allRooms.$promise
            .then(function(){
                $scope.allRooms.sort(self.sortRooms);

                $scope.allRooms.forEach(function(room){
                    room.participants.sort(ParticipantService.sortParticipant);
                });
            })
            .catch(GeneralErrorService.displayGeneralError);
    };

    this.joinRoom = function($scope, roomToJoin) {
        new Room({roomName:roomToJoin.roomName, participant:$scope.user})
            .$addParticipant()
            .then(function(){
                self.addMeToThisRoom($scope.user, $scope.allRooms, roomToJoin);
                self.addRoomToUserRoomsList($scope.user.chatRooms, roomToJoin.roomName);
            })
            .catch(GeneralErrorService.displayGeneralError);
    };

    this.quitRoom = function($scope, roomToLeave) {
        new Room({roomName:roomToLeave.roomName, participant:$scope.user})
            .$removeParticipant()
            .then(function(){
                self.removeMeFromThisRoom($scope.user, $scope.allRooms, roomToLeave);
                self.removeRoomFromUserRoomsList($scope.user.chatRooms, roomToLeave.roomName);
            })
            .catch(GeneralErrorService.displayGeneralError);
    };
});

killrChat.service('RoomCreationService', function(Room){

    this.createNewRoom = function($scope) {
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