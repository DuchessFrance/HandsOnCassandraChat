/**
 * Login page
 */

killrChat.controller('NavBarCtrl', function($rootScope, $scope){
    $rootScope
        .$on("$routeChangeError",function (event, current, previous, rejection) {
            $rootScope.accessDeniedMsg = rejection.data.message;
        });

    $scope.closeAlert = function() {
        delete $rootScope.accessDeniedMsg;
    };
});

killrChat.controller('SignInCtrl', function ($rootScope, $scope, $modal, $location,User) {

    $scope.username = null;
    $scope.password = null;
    $scope.rememberMe = false;
    $rootScope.user = new User();

    $scope.open = function () {
        var modalInstance = $modal.open({
            templateUrl: 'signUpModal.html',
            controller: 'SignUpModalCtrl'
        });

        //createdUser is an instance of the resource User
        modalInstance.result.then(function (createdUser) {
            $scope.username = createdUser.login;
            $scope.password = createdUser.password;
            $rootScope.user = createdUser;
            $rootScope.userCreated = true;
        });
    };

    $scope.login = function() {
        //call $login method on the resource User
        $rootScope.user.$login({
                j_username: $scope.username,
                j_password: $scope.password,
                _spring_security_remember_me: $scope.rememberMe
            },
            function() {
                $rootScope.user.login = $scope.username;

                // Remove user password now for security purpose!
                delete $rootScope.user.password;

                // Reset any previous error
                delete $scope.loginError;

                //Switch to chat view
                $location.path('/chat');

            },function(httpResponse){
                $scope.loginError = httpResponse.data.message;
            })
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
        $scope.user.$create(function(){
            delete $scope.user.passwordConfirm;
            $modalInstance.close($scope.user);
        },function(error) {
            $scope.user_create_error = error.data;
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss();
    };
});