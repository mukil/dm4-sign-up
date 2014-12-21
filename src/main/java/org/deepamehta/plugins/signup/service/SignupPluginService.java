package org.deepamehta.plugins.signup.service;

import de.deepamehta.core.service.PluginService;


/**
 *
 * @name dm4-sign-up
 * @website https://github.com/mukil/dm4-sign-up
 * @version 1.0.0-SNAPSHOT
 * @author <a href="mailto:malte@mikromedia.de">Malte Reissig</a>;
 */

public interface SignupPluginService extends PluginService {

    /** 
     * Checks for a Topic with the exact "username" value. 
     * 
     * @return  String  JSON-Object with property "isAvailable" set to true or false
     */
    String getUsernameAvailability(String username);
    
    /** 
     * Checks for username availability, password strength (>=8 chars) and assignes 
     * an "E Mail Address" to the new "User Account"-Topic.
     * 
     * @param   password    String which must have the prefix "-SHA-256-" and which must be encoded already.
     * 
     * @return  String  username
     */
    String createSimpleUserAccount(String username, String mailbox, String password);

}
