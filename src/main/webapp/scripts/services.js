killrChat.service('ChatService', function(){

    this.findMatchingRoom = function(rooms,matchingRoom) {
        return rooms.filter(function(room){
            return matchingRoom.roomName == room.roomName;
        })[0];
    };

    this.removeMatchingParticipant = function(participants, matchingParticipant) {

        var indexToRemove = -1;
        angular.forEach(participants, function(participant,index){
            if(participant.login == matchingParticipant.login) {
                indexToRemove = index;
                return;
            }
        });
        participants.splice(indexToRemove, 1);
    };


    this.addParticipant = function(participant, allRoomsList, participantRoomsList, targetRoom) {
        var roomInExistingList = this.findMatchingRoom(allRoomsList, targetRoom);

        roomInExistingList.participants.push({
            login:participant.login,
            firstname:participant.firstname,
            lastname:participant.lastname
        });

        var copy = angular.copy(targetRoom);
        delete copy.participants;
        participantRoomsList.push(copy);
    };

    this.removeParticipant = function(participant, allRoomsList, participantRoomsList, targetRoom) {

        var indexToRemove = -1;
        angular.forEach(participantRoomsList, function(userRoom,index){
            if(userRoom.roomName == targetRoom.roomName) {
                indexToRemove = index;
                return;
            }
        });

        participantRoomsList.splice(indexToRemove,1);

        var roomInExistingList = this.findMatchingRoom(allRoomsList, targetRoom);

        if(roomInExistingList) {
            angular.forEach(roomInExistingList.participants, function(participant,index){
                if(participant.login == participant.login) {
                    indexToRemove = index;
                    return;
                }
            });
            targetRoom.participants.splice(indexToRemove,1);
        }
    }
});
