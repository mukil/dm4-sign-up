package org.deepamehta.plugins.signup.service;


import com.sun.jersey.api.view.Viewable;

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
     *
     * @return  String  Workspace Topic ID
     */
    String createCustomWorkspaceMembershipRequest();

    /** 
     * Handles a sign-up request in regards to whether an Email based confirmation process is configured (true|false)
     * in the resp. <code>Sign-up Configuration</code> topic.
     *
     * To check whether a username is already taken you *must* use the getUsernameAvailability() call before issueing
     * an account creation request via this method.
     *
     * @param   username    String Unique username.
     * @param   password    String SHA256 encoded password with a prefix of "-SHA26-"
     * @param   mailbox     String containing a valid Email address related to the account creation request.
     * 
     * @return  String  username
     */
    Viewable handleSignupRequest(String username, String password, String mailbox);

    void sendSystemMailboxNotification(String subject, String message);

}
