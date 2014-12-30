killrChat.factory('User', function($resource) {
    return $resource('/users', [],{
        'create': {url: '/users', method: 'POST', isArray:false, headers:{'Content-Type': 'application/json'}},
        'login': {url: '/authenticate', method: 'POST', isArray:false, headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            transformRequest: function(){return {} }},
        'load': {url : '/users/:login', method:'GET', isArray:false, headers:{'Accept': 'application/json'}},
        'logout': {url: '/logout', method: 'GET', isArray:false, headers:{'Accept': 'application/json'}}
    });
});

killrChat.factory('Room', function($resource) {
    return $resource('/rooms', [],{
        'create': {url: '/rooms', method: 'POST', isArray:false,headers: {'Content-Type': 'application/json'}},
        "addParticipant": {url: '/rooms/user', method: 'PUT', isArray:false,headers: {'Content-Type': 'application/json'}},
        'removeParticipant': {url: '/rooms/user/remove', method: 'PUT', isArray:false,headers: {'Content-Type': 'application/json'}},
        'load': {url: '/rooms/:roomName', method: 'GET', isArray:false, headers:{'Accept': 'application/json'}},
        'list': {url: '/rooms/list', method: 'GET', isArray:true, headers:{'Accept': 'application/json'}}
    });
});

killrChat.factory('Message', function($resource) {
    return $resource('/messages', [],{
        'create': {url: '/messages/:roomName', method: 'POST', isArray:false,headers: {'Content-Type': 'application/json'}},
        'load': {url: '/messages/:roomName', method: 'GET', isArray:true, headers:{'Accept': 'application/json'}}
    });
});