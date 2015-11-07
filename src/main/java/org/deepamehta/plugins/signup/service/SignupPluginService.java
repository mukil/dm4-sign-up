package org.deepamehta.plugins.signup.service;



/**
 *
 * @name dm4-sign-up
 * @website https://github.com/mukil/dm4-sign-up
 * @version 1.1-SNAPSHOT
 * @author <a href="mailto:malte@mikromedia.de">Malte Reissig</a>;
 */

public interface SignupPluginService {

    /** 
     * Checks for a Topic with the exact "username" value. 
     * 
     * @return  String  JSON-Object with property "isAvailable" set to true or false
     */
    String getUsernameAvailability(String username);
    
    /** 
     * Creates a token for username and "E Mail Address" for the creation of a 
     * new "User Account" topic.
     * 
     * @param   password    String Password in cleartext.
     * 
     * @return  String  username
     */
    String createUserValidationToken(String username, String password, String mailbox);

}
