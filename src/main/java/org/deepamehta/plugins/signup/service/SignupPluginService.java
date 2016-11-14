package org.deepamehta.plugins.signup.service;


import com.sun.jersey.api.view.Viewable;
import org.osgi.framework.Bundle;

/**
 * A plugin service to check username or mailbox availability and to send
 * out system or user mailbox notifications.
 * @version 1.5.1
 * @author Malte Rei&szlig;
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
    String createAPIWorkspaceMembershipRequest();

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

    boolean isValidEmailAddress(String value);

    boolean isMailboxTaken(String value);

    /** Send notification email to system administrator mailbox configured in current \"Sign-up Configuration\" topic.*/
    void sendSystemMailboxNotification(String subject, String message);

    /** Send notification email to all mailboxes in String (many are seperated by a simple ";" and without spaces. */
    void sendUserMailboxNotification(String mailboxes, String subject, String message);

    /**
     * IMPORTANT: If you register your own bundle as a resource for thymeleaf templates you must call
     * reinitTemplateEngine afterwards.
     */
    void addTemplateResolverBundle(Bundle bundle);

    void removeTemplateResolverBundle(Bundle bundle);

    void reinitTemplateEngine();

}
