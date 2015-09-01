
(function ($, dm4c) {

    dm4c.add_plugin('org.deepamehta.sign-up', function () {

        dm4c.add_listener("init_3", function() {

            if (!isLoggedIn()) {
                var $sign_on = jQuery('<a href="/sign-up" id="sign-up-button">Sign up</a>')
                jQuery($sign_on).insertBefore("#login-widget a")
                jQuery('<span>&nbsp;</span>').insertAfter('#sign-up-button')
            }

                function isLoggedIn() {
                    var requestUri = '/accesscontrol/user'
                    //
                    var response = false
                    $.ajax({
                        type: "GET", url: requestUri,
                        dataType: "text", processData: true, async: false,
                        success: function(data, text_status, jq_xhr) {
                            if (typeof data === "undefined") return false
                            if (data != "") response = true
                        },
                        error: function(jq_xhr, text_status, error_thrown) {
                            console.log("Error performing GET request.. ")
                            response = false
                        }
                    })

                    return response
                }
        })

        // === Access Control Listeners ===

        dm4c.add_listener("logged_in", function(username) {
            // console.log("Sign-up plugin recevied LOGIN - removing sign-up-button")
            jQuery('#sign-up-button').remove()
        })

        dm4c.add_listener("logged_out", function() {
            if (jQuery('#sign-up-button').length <= 0) {
                var $sign_on = jQuery('<a href="/sign-up" id="sign-up-button">Sign up</a>')
                jQuery($sign_on).insertBefore("#login-widget a")
                jQuery('<span>&nbsp;</span>').insertAfter('#sign-up-button')
            } else {
                console.log("Sign-up button already present .. NO CHANGE")
            }
        })
    })

}(jQuery, dm4c))
