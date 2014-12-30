killrChat.service('ChatService', function(){

    this.findMatchingRoom = function(rooms,matchingRoom) {
        return rooms.filter(function(room){
            return matchingRoom.roomName == room.roomName;
        })[0];
    };

    this.removeMatchingParticipant = function(participants, matchingParticipant) {
        var indexToRemove = participants.map(function(p){return p.login}).indexOf(matchingParticipant.login);
        participants.splice(indexToRemove, 1);
    };


    this.addParticipantToRoom = function(participant, allRoomsList, targetRoom) {
        var roomInExistingList = this.findMatchingRoom(allRoomsList, targetRoom);

        roomInExistingList.participants.push({
            login:participant.login,
            firstname:participant.firstname,
            lastname:participant.lastname
        });
    };

    this.addRoomToUserRoomsList = function(userRooms, roomToJoin) {
        var indexOf = userRooms.indexOf(roomToJoin);
        if(indexOf == -1){
            userRooms.push(roomToJoin);
        }
    };

    this.removeParticipantFromRoom = function(participant, allRoomsList, targetRoom) {
        var roomInExistingList = this.findMatchingRoom(allRoomsList, targetRoom);
        if(roomInExistingList) {
            this.removeMatchingParticipant(roomInExistingList.participants, participant);
        }
    }

    this.removeRoomFromUserRoomsList = function(userRooms, roomToLeave) {
        var indexOf = userRooms.indexOf(roomToLeave);
        if(indexOf > -1){
            userRooms.splice(indexOf,1);
        }
    }
});
