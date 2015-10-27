
dm4c.add_plugin('org.deepamehta.sign-up', function () {

    var aclPlugin = dm4c.get_plugin("de.deepamehta.accesscontrol")

    dm4c.add_listener("init_3", function() {
        if (!isLoggedIn()) {
            render_sign_up_button()
        }
    })

    dm4c.add_listener("authority_decreased", function() {
        if (!isLoggedIn()) {
            render_sign_up_button()
        }
    })

    function isLoggedIn() {
        var username = aclPlugin.get_username()
        return (typeof username !== "undefined")
    }

    function render_sign_up_button () {
       var $sign_on = jQuery('<a href="/sign-up" id="sign-up-button">Sign up</a>')
        jQuery("#login-widget").prepend($sign_on)
    }

})
