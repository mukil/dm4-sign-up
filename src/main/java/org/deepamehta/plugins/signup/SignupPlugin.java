package org.deepamehta.plugins.signup;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Association;
import de.deepamehta.core.ChildTopics;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.DeepaMehtaEvent;
import de.deepamehta.core.service.CoreService;
import de.deepamehta.core.service.EventListener;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Transactional;
import de.deepamehta.core.service.accesscontrol.AccessControl;
import de.deepamehta.core.service.accesscontrol.Credentials;
import de.deepamehta.core.service.event.PostUpdateTopicListener;
import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.thymeleaf.ThymeleafPlugin;
import de.deepamehta.workspaces.WorkspacesService;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.mail.HtmlEmail;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.deepamehta.plugins.signup.service.SignupPluginService;

/**
 * This plugin enables anonymous users to create themselves a user account in DeepaMehta 4
 * through an Email based confirmation workflow and thus it critically depends on a postfix
 * like "internet" setup on "localhost".
 *
 * Routes registerd by this plugin are:
 * "/sign-up"				signup-form (registration dialog)
 * "/sign-up/login"			login-form (frontpage, alternate logind dialog)
 * "/sign-up/ok"			info-page after sucessfull account creation
 * "/sign-up/error"			info-page after failure (no account creation)
 * "/sign-up/token-info"	info-page notifying about confirmation mail
 *
 * @name dm4-sign-up
 * @website https://github.com/mukil/dm4-sign-up
 * @version 1.1-SNAPSHOT
 * @author <a href="mailto:malte@mikromedia.de">Malte Reissig</a>;
 */
@Path("/sign-up")
public class SignupPlugin extends ThymeleafPlugin implements SignupPluginService, PostUpdateTopicListener {

    private static Logger log = Logger.getLogger(SignupPlugin.class.getName());

    // --- DeepaMehta 4 related URIs --- //
    public static final String MAILBOX_TYPE_URI = "dm4.contacts.email_address";
    public static final String DM4_HOST_URL = System.getProperty("dm4.host.url");
    public static final boolean DM4_ACCOUNTS_ENABLED = Boolean.parseBoolean(System.getProperty("dm4.security" +
            ".new_accounts_are_enabled"));
    public static final String CONFIG_TOPIC_ACCOUNT_ENABLED = "dm4.accesscontrol.login_enabled";

    // --- Sign-up related type URIs (Configuration, Template Data) --- //
    private final String SIGN_UP_PLUGIN_TOPIC_URI   = "org.deepamehta.sign-up";
    private final String USER_MAILBOX_EDGE_TYPE     = "org.deepamehta.signup.user_mailbox";
    private final String SIGN_UP_CONFIG_TYPE_URI    = "org.deepamehta.signup.configuration";
    private final String CONFIG_PROJECT_TITLE       = "org.deepamehta.signup.config_project_title";
    private final String CONFIG_WEBAPP_TITLE        = "org.deepamehta.signup.config_webapp_title";
    private final String CONFIG_LOGO_PATH           = "org.deepamehta.signup.config_webapp_logo_path";
    private final String CONFIG_CSS_PATH            = "org.deepamehta.signup.config_custom_css_path";
    private final String CONFIG_READ_MORE_URL       = "org.deepamehta.signup.config_read_more_url";
    private final String CONFIG_PAGES_FOOTER        = "org.deepamehta.signup.config_pages_footer";
    private final String CONFIG_TOS_LABEL           = "org.deepamehta.signup.config_tos_label";
    private final String CONFIG_TOS_DETAILS         = "org.deepamehta.signup.config_tos_detail";
    private final String CONFIG_PD_LABEL            = "org.deepamehta.signup.config_pd_label";
    private final String CONFIG_PD_DETAILS          = "org.deepamehta.signup.config_pd_detail";
    private final String CONFIG_FROM_MAILBOX        = "org.deepamehta.signup.config_from_mailbox";
    private final String CONFIG_ADMIN_MAILBOX       = "org.deepamehta.signup.config_admin_mailbox";
    private final String CONFIG_EMAIL_CONFIRMATION  = "org.deepamehta.signup.config_email_confirmation";
    private final String CONFIG_START_PAGE_URL      = "org.deepamehta.signup.start_page_url";
    private final String CONFIG_HOME_PAGE_URL       = "org.deepamehta.signup.home_page_url";
    private final String CONFIG_LOADING_HINT        = "org.deepamehta.signup.loading_app_hint";
    private final String CONFIG_LOGGING_OUT_HINT    = "org.deepamehta.signup.logging_out_hint";
    private final String CONFIG_API_ENABLED         = "org.deepamehta.signup.config_api_enabled";
    private final String CONFIG_API_DESCRIPTION     = "org.deepamehta.signup.config_api_description";
    private final String CONFIG_API_DETAILS         = "org.deepamehta.signup.config_api_details";
    private final String CONFIG_API_WORKSPACE_URI   = "org.deepamehta.signup.config_api_workspace_uri";


    private Topic activeModuleConfiguration = null;
    private Topic customWorkspaceAssignmentTopic = null;

    @Inject private AccessControlService acService;

    @Inject private WorkspacesService wsService;

    @Context UriInfo uri;

    HashMap<String, JSONObject> token = new HashMap<String, JSONObject>();
    HashMap<String, JSONObject> pwToken = new HashMap<String, JSONObject>();

    @Override
    public void init() {
        initTemplateEngine();
        reloadActiveSignupConfiguration();
    }

    /**
     * Custom event fired by sign-up module up on successful user account creation.
     *
     * @return Topic	The username topic (related to newly created user account
     * topic).
     */
    static DeepaMehtaEvent USER_ACCOUNT_CREATE_LISTENER = new DeepaMehtaEvent(UserAccountCreateListener.class) {
        @Override
        public void dispatch(EventListener listener, Object... params) {
            ((UserAccountCreateListener) listener).userAccountCreated((Topic) params[0]);
        }
    };



    // --- Plugin Service Implementation --- //

    @GET
    @Path("/check/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUsernameAvailability(@PathParam("username") String username) {
        JSONObject response = new JSONObject();
        try {
            response.put("isAvailable", true);
            if (isUsernameTaken(username)) {
                response.put("isAvailable", false);
            }
            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/check/mailbox/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMailboxAvailability(@PathParam("email") String email) {
        JSONObject response = new JSONObject();
        try {
            response.put("isAvailable", true);
            if (isMailboxTaken(email)) {
                response.put("isAvailable", false);
            }
            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/password-token/{email}")
    @Produces(MediaType.TEXT_HTML)
    public Response initiatePasswordReset(@PathParam("email") String email) throws URISyntaxException {
        log.info("Password reset requested for user with Email: \"" + email + "\"");
        try {
            String emailAddressValue = email.trim();
            boolean emailExists = dm4.getAccessControl().emailAddressExists(emailAddressValue);
            if (emailExists) {
                log.info("Email based password reset workflow do'able, sending out passwort reset mail.");
                createPasswordResetToken(emailAddressValue);
                return Response.temporaryRedirect(new URI("/sign-up/token-info")).build();
            } else {
                log.info("Email based password reset workflow not do'able, Email Addresses does not exist.");
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(SignupPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.temporaryRedirect(new URI("/sign-up/error")).build();
    }

    @GET
    @Path("/password-reset/{token}")
    @Produces(MediaType.TEXT_HTML)
    public Viewable handlePasswordResetRequest(@PathParam("token") String token) {
        try {
            // 1) Assert token exists: It may not exist due to e.g. bundle refresh, system restart, token invalid
            if (!pwToken.containsKey(token)) {
                viewData("username", null);
                viewData("message", "Sorry, the link is invalid");
            }
            // 2) Process available token and remove it from stack
            String username, email;
            JSONObject input = pwToken.get(token);
            // 3) Update the user account credentials OR present an error message.
            prepareSignupPage();
            viewData("status", "updated");
            viewData("token", token);
            if (input != null && input.getLong("expiration") > new Date().getTime()) {
                username = input.getString("username");
                email = input.getString("mailbox");
                log.info("Handling password reset request for Email: \"" + email);
                viewData("username", username);
                return view("password-reset");
            } else {
                log.warning("Sorry the link to reset the password for ... has expired.");
                viewData("message", "Sorry, the link to reset the password has expired.");
                viewData("status", "updated");
                return view("failure");
            }
        } catch (JSONException ex) {
            log.severe("Sorry, an error occured during retriving your token. Please try again. " + ex.getMessage());
            viewData("message", "Sorry, an error occured during the handling of the reset password link.");
            return view("failure");
        }
    }

    @GET
    @Path("/password-reset/{token}/{password}")
    @Transactional
    public Viewable processPasswordUpdateRequest(@PathParam("token") String token, @PathParam("password") String password) {
        log.info("Processing Password Update Request Token... ");
        prepareSignupPage();
        viewData("status", "updated");
        try {
            JSONObject entry = pwToken.get(token);
            if (entry != null) {
                    Credentials newCreds = new Credentials("dummy", "pass");
                    newCreds.username = entry.getString("username");
                    newCreds.password = password;
                    dm4.getAccessControl().changePassword(newCreds);
                    pwToken.remove(token);
                    log.info("Credentials for user " + newCreds.username + " were changed succesfully.");
                    viewData("message", "Your password was succesfully updated.");
                    return view("password-ok");
            } else {
                viewData("message", "Sorry, an error occured while updating the credentials.");
                return view("failure");
            }
        } catch (JSONException ex) {
            Logger.getLogger(SignupPlugin.class.getName()).log(Level.SEVERE, null, ex);
            viewData("message", "Sorry, an error occured while updating the credentials.");
            return view("failure");
        }
    }

    @GET
    @Path("/handle/{username}/{pass-one}/{mailbox}")
    public Viewable handleSignupRequest(@PathParam("username") String username,
            @PathParam("pass-one") String password, @PathParam("mailbox") String mailbox) throws WebApplicationException {
        try {
            if (activeModuleConfiguration.getChildTopics().getBoolean(CONFIG_EMAIL_CONFIRMATION)) {
                log.info("Sign-up Configuration: Email based confirmation workflow active, send out confirmation mail.");
                createUserValidationToken(username, password, mailbox);
                // redirect user to a "token-info" page
                throw new WebApplicationException(Response.temporaryRedirect(new URI("/sign-up/token-info")).build());
            } else {
                createSimpleUserAccount(username, password, mailbox);
                if (DM4_ACCOUNTS_ENABLED) {
                    log.info("Sign-up Configuration: Email based confirmation workflow inactive."
                        + "The new account is ENABLED.");
                    // redirecting user to the "your account is now active" page
                    prepareSignupPage();
                    throw new WebApplicationException(Response.temporaryRedirect(new URI("/sign-up/"+username+"/ok")).build());
                } else {
                    log.info("Sign-up Configuration: Email based confirmation workflow inactive but new user account " +
                            "created is DISABLED.");
                    // redirecting to page displaying "your account was created but needs to be activated"
                    throw new WebApplicationException(Response.temporaryRedirect(new URI("/sign-up/pending")).build());
                }
            }
        } catch (URISyntaxException e) {
            log.log(Level.SEVERE, "Could not build response URI while handling sign-up request", e);
        }
        return getFailureView();
    }

    @GET
    @Path("/confirm/{token}")
    public Viewable processSignupRequest(@PathParam("token") String key) {
        // 1) Assert token exists: It may not exist due to e.g. bundle refresh, system restart, token invalid
        if (!token.containsKey(key)) {
            viewData("username", null);
            viewData("message", "Sorry, the link is invalid");
            return getFailureView();
        }
        // 2) Process available token and remove it from stack
        String username;
        JSONObject input = token.get(key);
        token.remove(key);
        // 3) Create the user account and show ok OR present an error message.
        try {
            username = input.getString("username");
            if (input.getLong("expiration") > new Date().getTime()) {
                log.log(Level.INFO, "Trying to create user account for {0}", input.getString("mailbox"));
                createSimpleUserAccount(username, input.getString("password"), input.getString("mailbox"));
            } else {
                viewData("username", null);
                viewData("message", "Sorry, the link has expired");
                return getFailureView();
            }
        } catch (JSONException ex) {
            Logger.getLogger(SignupPlugin.class.getName()).log(Level.SEVERE, null, ex);
            viewData("message", "An error occured processing your request");
            log.log(Level.SEVERE, "Account creation failed due to {0} caused by {1}",
                new Object[]{ex.getMessage(), ex.getCause().toString()});
            return getFailureView();
        }
        log.log(Level.INFO, "Account succesfully created for username: {0}", username);
        viewData("username", username);
        viewData("message", "User account created successfully");
        if (!DM4_ACCOUNTS_ENABLED) {
            log.log(Level.INFO, "> Account activation by an administrator remains PENDING ");
            return getAccountCreationPendingView();
        }
        return getAccountCreationOKView(username);
    }

    @POST
    @Path("/confirm/membership/custom")
    @Transactional
    @Override
    public String createAPIWorkspaceMembershipRequest() {
        Topic apiMembershipRequestNote = dm4.getTopicByUri("org.deepamehta.signup.api_membership_requests");
        Topic signupConfiguration = getCurrentSignupConfiguration();
        if (apiMembershipRequestNote != null && acService.getUsername() != null) {
            Topic usernameTopic = acService.getUsernameTopic(acService.getUsername());
            Association requestRelation = getDefaultAssocation(usernameTopic.getId(), apiMembershipRequestNote.getId());
            String apiWorkspaceUri = signupConfiguration.getChildTopics().getString(CONFIG_API_WORKSPACE_URI);
            if (!apiWorkspaceUri.isEmpty()) {
                Topic apiWorkspace = dm4.getAccessControl().getWorkspace(apiWorkspaceUri);
                if (requestRelation == null) {
                    // ### Take care of the correct Workspace Assignment for this Assocation/Relation
                    // ### acService.createMembership(usernameTopic.getSimpleValue().toString(), workspace.getId());
                    dm4.createAssociation(mf.newAssociationModel("dm4.core.association", mf.newTopicRoleModel(usernameTopic.getId(), "dm4.core.default"),
                        mf.newTopicRoleModel(apiMembershipRequestNote.getId(), "dm4.core.default")));
                    log.info("Request for new custom Workspace Membership by " + usernameTopic.getSimpleValue().toString());
                    sendSystemMailboxNotification("Custom Workspace Membership Requested", "\nHi admin,\n\n"
                        + usernameTopic.getSimpleValue().toString() + " accepted the Terms of Service and confirmed membership in Workspace \""
                        + apiWorkspace.getSimpleValue().toString() + "\"\n\nJust wanted to let you know.\nCheers!");
                } else {
                    log.info("Revoke Request for custom Workspace Membership by " + usernameTopic.getSimpleValue().toString());
                    sendSystemMailboxNotification("Custom Workspace Membership Revoked", "\nHi admin,\n\n"
                        + usernameTopic.getSimpleValue().toString() + " just revoked the membership in Workspace \""
                        + apiWorkspace.getSimpleValue().toString() + "\"\n\nJust wanted to let you know.\nCheers!");
                    dm4.deleteAssociation(requestRelation.getId());
                }
                return "{ \"membership_created\" : " + true + "}";
            } else {
                log.warning("No API Workspace Configured: You must enter the URI of your API Workspace"
                    + " into your current \"Signup Configuration\".");
                return "{ \"membership_created\" : " + false + "}";
            }
        } else {
            return "{ \"membership_created\" : " + false + "}";
        }
    }


    // --- Sign-up Plugin Routes --- //

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSignupFormView() {
        prepareSignupPage();
        if (acService.getUsername() != null) return view("logout");
        return view("sign-up");
    }

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getLoginView() {
        prepareSignupPage();
        if (acService.getUsername() != null) return view("logout");
        return view("login");
    }

    @GET
    @Path("/request-password")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPasswordResetView() {
        prepareSignupPage();
        return view("request-password");
    }

    @GET
    @Path("/{username}/ok")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAccountCreationOKView(@PathParam("username") String username) {
        prepareSignupPage();
        viewData("username", username);
        return view("ok");
    }

    @GET
    @Path("/pending")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAccountCreationPendingView() {
        prepareSignupPage();
        return view("pending");
    }

    @GET
    @Path("/error")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getFailureView() {
        prepareSignupPage();
        return view("failure");
    }

    @GET
    @Path("/token-info")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getConfirmationInfoView() {
        prepareSignupPage();
        return view("confirmation");
    }

    @GET
    @Path("/edit")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAccountDetailsView() {
        prepareSignupPage();
        prepareAccountEditPage();
        return view("edit");
    }

    @Override
    public void sendSystemMailboxNotification(String subject, String message) {
        if (activeModuleConfiguration.getChildTopics().getTopicOrNull(CONFIG_ADMIN_MAILBOX) != null && // 4.8 migration
            !activeModuleConfiguration.getChildTopics().getString(CONFIG_ADMIN_MAILBOX).isEmpty()) {
            String recipient = activeModuleConfiguration.getChildTopics().getString(CONFIG_ADMIN_MAILBOX);
            sendSystemMail(subject, message, recipient);
        } else {
            log.warning("Did not send notification mail to System Mailbox - Admin Mailbox Empty");
        }
    }

    @Override
    public void sendUserMailboxNotification(String mailbox, String subject, String message) {
        sendSystemMail(subject, message, mailbox);
        /** if (dm4.getAccessControl().emailAddressExists(mailbox)) { // is a mailbox of a user
            sendSystemMail(subject, message, mailbox);
        } else {
            log.warning("Did not send notification mail to System Mailbox - User with Mailbox ("+mailbox+") not known");
        }*/
    }

    // --- Private Helpers --- //

    private void createUserValidationToken(String username, String password, String mailbox) {
        try {
            String key = UUID.randomUUID().toString();
            long valid = new Date().getTime() + 3600000; // Token is valid fo 60 min
            JSONObject value = new JSONObject()
                    .put("username", username.trim())
                    .put("mailbox", mailbox.trim())
                    .put("password", password)
                    .put("expiration", valid);
            token.put(key, value);
            log.log(Level.INFO, "Set up key {0} for {1} sending confirmation mail valid till {3}",
                    new Object[]{key, mailbox, new Date(valid).toString()});
            // ### TODO: if sending confirmation mail fails users should know about that and
            // get to see the "failure" screen next (with a proper message)
            sendConfirmationMail(key, username, mailbox.trim());
        } catch (JSONException ex) {
            Logger.getLogger(SignupPlugin.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    private void createPasswordResetToken(String mailbox) {
        String username = dm4.getAccessControl().getUsername(mailbox);
        try {
            String key = UUID.randomUUID().toString();
            long valid = new Date().getTime() + 3600000; // Token is valid fo 60 min
            JSONObject value = new JSONObject()
                    .put("username", username.trim())
                    .put("mailbox", mailbox.trim())
                    .put("expiration", valid);
            pwToken.put(key, value);
            log.log(Level.INFO, "Set up pwToken {0} for {1} send passwort reset mail valid till {3}",
                    new Object[]{key, mailbox, new Date(valid).toString()});
            // ### TODO: if sending confirmation mail fails, users should know about that and
            // get to see the "failure" screen next (with a proper message)
            sendPasswordResetMail(key, username, mailbox.trim());
        } catch (JSONException ex) {
            Logger.getLogger(SignupPlugin.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    private String createSimpleUserAccount(String username, String password, String mailbox) {
        DeepaMehtaTransaction tx = dm4.beginTx();
        try {
            if (isUsernameTaken(username)) {
                // Might be thrown if two users compete for registration (of the same username)
                // within the same 60 minutes (tokens validity timespan). First confirming, wins.
                throw new RuntimeException("Username was already registered and confirmed!");
            }
            Credentials creds = new Credentials(new JSONObject()
                .put("username", username.trim())
                .put("password", password.trim()));
            // 1) Create new user (in which workspace), just within the private one, no?
            final Topic usernameTopic = acService.createUserAccount(creds);
            final String eMailAddressValue = mailbox;
            // 2) create and associate e-mail address topic
            dm4.getAccessControl().runWithoutWorkspaceAssignment(new Callable<Topic>() {
                @Override
                public Topic call() {
                    Topic eMailAddress = dm4.createTopic(mf.newTopicModel(MAILBOX_TYPE_URI,
                        new SimpleValue(eMailAddressValue)));
                    // 3) fire custom event ### this is useless since fired by "anonymous" (this request scope)
                    dm4.fireEvent(USER_ACCOUNT_CREATE_LISTENER, usernameTopic);
                    AccessControl acCore = dm4.getAccessControl();
                    // 4) assign new e-mail address topic to admins "Private workspace" // ### administration workspace
                    long adminWorkspaceId = dm4.getAccessControl().getAdministrationWorkspaceId();
                    acCore.assignToWorkspace(eMailAddress, adminWorkspaceId);
                    // 5) associate email address to "username" topic too
                    Association assoc = dm4.createAssociation(mf.newAssociationModel(USER_MAILBOX_EDGE_TYPE,
                        mf.newTopicRoleModel(eMailAddress.getId(), "dm4.core.child"),
                        mf.newTopicRoleModel(usernameTopic.getId(), "dm4.core.parent")));
                    // 6) assign that association also to admins "Private Workspace"
                    acCore.assignToWorkspace(assoc, adminWorkspaceId);
                    // 7) create membership to custom workspace topic
                    if (customWorkspaceAssignmentTopic != null) {
                        acService.createMembership(usernameTopic.getSimpleValue().toString(),
                                customWorkspaceAssignmentTopic.getId());
                        log.info("Created new Membership for " + usernameTopic.getSimpleValue().toString() + " in " +
                                "workspace=" + customWorkspaceAssignmentTopic.getSimpleValue().toString());
                    }
                    return eMailAddress;
                }
            });
            log.info("Created new user account for user \"" + username + "\" and " + eMailAddressValue);
            // 7) Inform administrations about successfull account creation
            sendNotificationMail(username, mailbox.trim());
            tx.success();
            return username;
        } catch (Exception e) {
            throw new RuntimeException("Creating simple user account FAILED!", e);
        } finally {
            tx.finish();
        }
    }

    /**
     * Loads the next sign-up configuration topic for this plugin.
     *
     * @see init()
     * @see postUpdateTopic()
     */
    private Topic reloadActiveSignupConfiguration() {
        log.info("Sign-up: Reloading sign-up plugin configuration and checking for custom workspace assignment assoc");
        activeModuleConfiguration = getCurrentSignupConfiguration();
        activeModuleConfiguration.loadChildTopics();
        // check for custom workspace assignment
        customWorkspaceAssignmentTopic = getCustomWorkspaceAssignmentTopic();
        if (customWorkspaceAssignmentTopic != null) {
            log.info("Configured Custom Sign-up Workspace => \""
                    + customWorkspaceAssignmentTopic.getSimpleValue() + "\"");
        }
        log.log(Level.INFO, "Sign-up: Loaded sign-up configuration => \"{0}\", \"{1}\"",
            new Object[]{activeModuleConfiguration.getUri(), activeModuleConfiguration.getSimpleValue()});
        return activeModuleConfiguration;
    }

    private void sendConfirmationMail(String key, String username, String mailbox) {
        try {
            String webAppTitle = activeModuleConfiguration.getChildTopics().getString(CONFIG_WEBAPP_TITLE);
            URL url = new URL(DM4_HOST_URL);
            log.info("The confirmation mails token request URL should be:"
                + "\n" + url + "sign-up/confirm/" + key);
            if (DM4_ACCOUNTS_ENABLED) {
                sendSystemMail("Your account on " + webAppTitle,
                    "Hi " + username + ",\n\nplease click the following link to activate your account. Your account " +
                            "is ready to use immediately.\n" + url + "sign-up/confirm/" + key
                        + "\n\nCheers!", mailbox);
            } else {
                sendSystemMail("Your account on " + webAppTitle,
                    "Hi " + username + ",\n\nplease click the following link to proceed with the sign-up process.\n"
                        + url + "sign-up/confirm/" + key
                        + "\n\n" + "You'll receive another mail once your account is activated by an " +
                        "administrator. This may need 1 or 2 days."
                        + "\n\nCheers!", mailbox);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** ### Untested */
    private void sendPasswordResetMail(String key, String username, String mailbox) {
        try {
            String webAppTitle = activeModuleConfiguration.getChildTopics().getString(CONFIG_WEBAPP_TITLE);
            URL url = new URL(DM4_HOST_URL);
            log.info("The password reset mails token request URL should be:"
                + "\n" + url + "sign-up/password-reset/" + key);
            sendSystemMail("Password Reset " + webAppTitle,
                "Hi " + username + ",\n\nplease click the following link to enter a new password for your account.\n"
                    + url + "sign-up/password-reset/" + key + "\n\nCheers!", mailbox);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void sendNotificationMail(String username, String mailbox) {
        String webAppTitle = activeModuleConfiguration.getChildTopics().getString(CONFIG_WEBAPP_TITLE);
        //
        if (activeModuleConfiguration.getChildTopics().getTopicOrNull(CONFIG_ADMIN_MAILBOX) != null &&
            !activeModuleConfiguration.getChildTopics().getString(CONFIG_ADMIN_MAILBOX).isEmpty()) {
            String adminMailbox = activeModuleConfiguration.getChildTopics().getString(CONFIG_ADMIN_MAILBOX);
                sendSystemMail("Account registration on " + webAppTitle,
                        "\nA user has registered.\n\nUsername: " + username + "\nEmail: " + mailbox, adminMailbox);
        } else {
            log.info("ADMIN: No \"Admin Mailbox\" configured: A new user account (" + username + ") was created but" +
                    " no notification sent (to sys-admin).");
        }
    }

    /**
     *
     * @param subject       String Subject text for the message.
     * @param message       String Text content of the message.
     * @param recipient     String of Email Address message is sent to **must not** be NULL.
     */
    private void sendSystemMail(String subject, String message, String recipient) {
        // Hot Fix: Classloader issue we have in OSGi since using Pax web
        Thread.currentThread().setContextClassLoader(SignupPlugin.class.getClassLoader());
        log.info("BeforeSend: Set classloader to " + Thread.currentThread().getContextClassLoader().toString());
        HtmlEmail email = new HtmlEmail();
        email.setDebug(true); // => System.out.println(SMTP communication);
        email.setHostName("localhost"); // ### use getBaseUri() from HTTP Context?
        try {
            // ..) Set Senders of Mail
            String projectName = activeModuleConfiguration.getChildTopics().getString(CONFIG_PROJECT_TITLE);
            String sender = activeModuleConfiguration.getChildTopics().getString(CONFIG_FROM_MAILBOX);
            email.setFrom(sender.trim(), projectName.trim());
            // ..) Set Subject of Mail
            email.setSubject(subject);
            // ..) Set Message Body and append the Host URL
            message += "\n\n" + DM4_HOST_URL + "\n\n";
            email.setTextMsg(message);
            // ..) Set recipient of notification mail
            String recipientValue = recipient.trim();
            log.info("Loaded current configuration topic, sending notification mail to " + recipientValue);
            Collection<InternetAddress> recipients = new ArrayList<InternetAddress>();
            if (recipientValue.contains(";")) {
                // ..) Many Recipients
                for (String recipientPart : recipientValue.split(";")) {
                    recipients.add(new InternetAddress(recipientPart.trim()));
                }
            } else {
                // ..) A Single Recipient
                recipients.add(new InternetAddress(recipientValue));
            }
            email.setTo(recipients);
            email.send();
            log.info("Mail was SUCCESSFULLY sent to " + email.getToAddresses() + " mail addresses");
        } catch (Exception ex) {
            throw new RuntimeException("Sending notification mail FAILED", ex);
        } finally {
            // Fix: Classloader issue we have in OSGi since using Pax web
            Thread.currentThread().setContextClassLoader(CoreService.class.getClassLoader());
            log.info("AfterSend: Set Classloader back " + Thread.currentThread().getContextClassLoader().toString());
        }
    }

    private boolean isUsernameTaken(String username) {
        String value = username.trim();
        Topic userNameTopic = acService.getUsernameTopic(value);
        return (userNameTopic != null);
    }

    private boolean isMailboxTaken(String email) {
        String value = email.toLowerCase().trim();
        return dm4.getAccessControl().emailAddressExists(value);
    }

    private Association getDefaultAssocation(long topic1, long topic2) {
        return dm4.getAssociation("dm4.core.association",  topic1, topic2, "dm4.core.default", "dm4.core.default");
    }

    /**
     * The sign-up configuration object is loaded once when this bundle/plugin
     * is initialized by the framework and as soon as one configuration was
     * edited.
     *
     * @see reloadConfiguration()
     */
    private Topic getCurrentSignupConfiguration() {
        Topic pluginTopic = dm4.getTopicByUri(SIGN_UP_PLUGIN_TOPIC_URI);
        return pluginTopic.getRelatedTopic("dm4.core.association", "dm4.core.default", "dm4.core.default",
                SIGN_UP_CONFIG_TYPE_URI);
    }

    private Topic getCustomWorkspaceAssignmentTopic() {
        // Note: It must always be just ONE workspace related to the current module configuration
        return activeModuleConfiguration.getRelatedTopic("dm4.core.association", "dm4.core.default",
                "dm4.core.default","dm4.workspaces.workspace");
    }

    private void prepareSignupPage() {
        if (activeModuleConfiguration != null) {
            ChildTopics configuration = activeModuleConfiguration.getChildTopics();
            viewData("title", configuration.getTopic(CONFIG_WEBAPP_TITLE).getSimpleValue().toString());
            viewData("logo_path", configuration.getTopic(CONFIG_LOGO_PATH).getSimpleValue().toString());
            viewData("css_path", configuration.getTopic(CONFIG_CSS_PATH).getSimpleValue().toString());
            viewData("project_name", configuration.getTopic(CONFIG_PROJECT_TITLE).getSimpleValue().toString());
            viewData("read_more_url", configuration.getTopic(CONFIG_READ_MORE_URL).getSimpleValue().toString());
            viewData("tos_label", configuration.getTopic(CONFIG_TOS_LABEL).getSimpleValue().toString());
            viewData("tos_details", configuration.getTopic(CONFIG_TOS_DETAILS).getSimpleValue().toString());
            viewData("pd_label", configuration.getTopic(CONFIG_PD_LABEL).getSimpleValue().toString());
            viewData("pd_details", configuration.getTopic(CONFIG_PD_DETAILS).getSimpleValue().toString());
            viewData("footer", configuration.getTopic(CONFIG_PAGES_FOOTER).getSimpleValue().toString());
            viewData("start_url", configuration.getTopic(CONFIG_START_PAGE_URL).getSimpleValue().toString());
            viewData("home_url", configuration.getTopic(CONFIG_HOME_PAGE_URL).getSimpleValue().toString());
            viewData("loading_app_hint", configuration.getTopic(CONFIG_LOADING_HINT).getSimpleValue().toString());
            viewData("logging_out_hint", configuration.getTopic(CONFIG_LOGGING_OUT_HINT).getSimpleValue().toString());
            viewData("custom_workspace_enabled", configuration.getBoolean(CONFIG_API_ENABLED));
            viewData("custom_workspace_description", configuration.getTopic(CONFIG_API_DESCRIPTION).getSimpleValue().toString());
            viewData("custom_workspace_details", configuration.getTopic(CONFIG_API_DETAILS).getSimpleValue().toString());
            viewData("custom_workspace_uri", configuration.getTopic(CONFIG_API_WORKSPACE_URI).getSimpleValue().toString());
            viewData("status", "created");
        } else {
            log.warning("Could not load module configuration of sign-up plugin during page preparation!");
        }
    }

    private void prepareAccountEditPage() {
        String username = acService.getUsername();
        if (username != null) {
            // Someone is logged in, prepare her account page has no permission to edit or view her mailbox
            // Topic mailbox = usernameTopic.getRelatedTopic(USER_MAILBOX_EDGE_TYPE, "dm4.core.parent",
                // "dm4.core.child", MAILBOX_TYPE_URI);
            String eMailAddressValue = "Email Address Hidden";
            viewData("logged_in", true);
            viewData("username", username);
            viewData("email", eMailAddressValue);
            viewData("link", "");
            // ### viewData("confirmed", true); // Check if user already has confirmed for a membership
        } else {
            // Not authenticated, can't do nothing but login
            viewData("logged_in", false);
            viewData("username", "Not logged in");
            viewData("email", "Not logged in");
            viewData("link", "/sign-up/login");
        }
    }

    public void postUpdateTopic(Topic topic, TopicModel tm, TopicModel tm1) {
        if (topic.getTypeUri().equals(SIGN_UP_CONFIG_TYPE_URI)) {
            reloadActiveSignupConfiguration();
        } else if (topic.getTypeUri().equals(CONFIG_TOPIC_ACCOUNT_ENABLED)) {
            // Account status
            boolean status = Boolean.parseBoolean(topic.getSimpleValue().toString());
            // Account involved
            Topic username = topic.getRelatedTopic("dm4.config.configuration", null,
                    null, "dm4.accesscontrol.username");
            // Perform notification
            if (status && !DM4_ACCOUNTS_ENABLED) { // Enabled=true && new_accounts_are_enabled=false
                log.info("Sign-up Notification: User Account \"" + username.getSimpleValue()+"\" is now ENABLED!");
                //
                String webAppTitle = activeModuleConfiguration.getChildTopics().getTopic(CONFIG_WEBAPP_TITLE)
                        .getSimpleValue().toString();
                Topic mailbox = username.getRelatedTopic(USER_MAILBOX_EDGE_TYPE, null, null, MAILBOX_TYPE_URI);
                if (mailbox != null) { // for accounts created via sign-up plugin this will always evaluate to true
                    String mailboxValue = mailbox.getSimpleValue().toString();
                    sendSystemMail("Your account on " + webAppTitle + " is now active",
                            "Hi " + username.getSimpleValue() + ",\n\nyour account on " + DM4_HOST_URL + " is now " +
                                    "active.\n\nCheers!", mailboxValue);
                    log.info("Send system notification mail to " + mailboxValue + " - The account is now active!");
                }
            }
        }
    }

}
