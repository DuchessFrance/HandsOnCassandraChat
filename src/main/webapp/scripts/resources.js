killrChat.factory('User', ['$resource', function($resource) {
    return $resource('/users', [],{
        'create': {url: '/users/create', method: 'POST', isArray:false, headers:{'Content-Type': 'application/json'}},
        'login': {url: '/authenticate', method: 'POST', isArray:false,
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            transformRequest: function(){return {} }},
        'changePassword': {url: '/users/password', method: 'PUT', isArray:false, headers:{'Content-Type': 'application/json'}},
        'listRooms': {url: '/users/rooms', method: 'GET', isArray:true, headers:{'Accept': 'application/json'}}
    });
}]);