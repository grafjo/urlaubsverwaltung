$(document).ready(function() {

    var url = document.URL;

    if(url.indexOf('login_error') != -1) {
        $('#login--error').show('drop', {direction: 'up'});
    }
});