killrChat.service('ChatService', function(){

    this.findMatchingRoom = function(rooms,matchingRoom) {
        return rooms.filter(function(room){
            return matchingRoom.roomName == room.roomName;
        })[0];
    };

    this.sortParticipant = function(participantA,participantB){
        return participantA.firstname.localeCompare(participantB.firstname);
    };

    /**
     * Room participants list
     *
     */
    this.addParticipantToCurrentRoom = function(currentRoom, participantToAdd) {
        currentRoom.participants.push(participantToAdd);
        currentRoom.participants.sort(this.sortParticipant);
    };

    this.removeParticipantFromCurrentRoom = function(currentRoom, participantToRemove) {
        var indexToRemove = currentRoom.participants.map(function(p){return p.login}).indexOf(participantToRemove.login);
        currentRoom.participants.splice(indexToRemove, 1);
    };


    this.addMeToThisRoom = function(me, allRoomsList, targetRoom) {
        var roomInExistingList = this.findMatchingRoom(allRoomsList, targetRoom);

        roomInExistingList.participants.push({
            login:me.login,
            firstname:me.firstname,
            lastname:me.lastname
        });
        roomInExistingList.participants.sort(this.sortParticipant);
    };


    this.removeMeFromThisRoom = function(participant, allRoomsList, targetRoom) {
        var roomInExistingList = this.findMatchingRoom(allRoomsList, targetRoom);
        if(roomInExistingList) {
            this.removeParticipantFromCurrentRoom(roomInExistingList, participant);
        }
    }

    /**
     * User rooms list
     *
     */

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
