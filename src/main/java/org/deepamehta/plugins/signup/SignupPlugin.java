package org.deepamehta.plugins.signup;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Association;
import de.deepamehta.core.ChildTopics;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.DeepaMehtaEvent;
import de.deepamehta.core.service.DeepaMehtaService;
import de.deepamehta.core.service.EventListener;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.ResultList;
import de.deepamehta.core.service.Transactional;
import de.deepamehta.core.service.accesscontrol.AccessControl;
import de.deepamehta.core.service.accesscontrol.Credentials;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
import de.deepamehta.plugins.workspaces.service.WorkspacesService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.deepamehta.plugins.signup.service.SignupPluginService;


/**
 * Routes registerd by this plugin are:
 *
 * /        => login-form
 * /sign-up => signup-form
 * /ok      => info-page after account creation
 *
 * @name dm4-sign-up
 * @website https://github.com/mukil/dm4-sign-up
 * @version 1.1-SNAPSHOT
 * @author <a href="mailto:malte@mikromedia.de">Malte Reissig</a>;
 */

@Path("/")
public class SignupPlugin extends WebActivatorPlugin implements SignupPluginService {

    private static Logger log = Logger.getLogger(SignupPlugin.class.getName());

    // --- DeepaMehta 4 related type URIs
    
    /** @see also @de.deepamehta.plugins.accesscontrol.model.Credentials */
    private static final String ENCRYPTED_PASSWORD_PREFIX = "-SHA256-";

    public static final String USER_ACCOUNT_TYPE_URI = "dm4.accesscontrol.user_account";
    public static final String MAILBOX_TYPE_URI = "dm4.contacts.email_address";

    private static final String USERNAME_TYPE_URI = "dm4.accesscontrol.username";
    // private static final String USER_PASSWORD_TYPE_URI = "dm4.accesscontrol.password";
    // private static final String WS_WIKIDATA_URI = "org.deepamehta.workspaces.wikidata";
    // private static final String WS_DEFAULT_URI = "de.workspaces.deepamehta";

    public final static String WS_DM_DEFAULT_URI = "de.workspaces.deepamehta";
    
    // --- Sign-up related type URIs (Configuration, Template Data)
    
    private final String CONFIG_PROJECT_TITLE = "org.deepamehta.signup.config_project_title";
    private final String CONFIG_WEBAPP_TITLE = "org.deepamehta.signup.config_webapp_title";
    private final String CONFIG_LOGO_PATH = "org.deepamehta.signup.config_webapp_logo_path";
    private final String CONFIG_CSS_PATH = "org.deepamehta.signup.config_custom_css_path";
    private final String CONFIG_READ_MORE_URL = "org.deepamehta.signup.config_read_more_url";
    private final String CONFIG_PAGES_FOOTER = "org.deepamehta.signup.config_pages_footer";
    private final String CONFIG_TOS_LABEL = "org.deepamehta.signup.config_tos_label";
    private final String CONFIG_TOS_DETAILS = "org.deepamehta.signup.config_tos_detail";
    private final String CONFIG_PD_LABEL = "org.deepamehta.signup.config_pd_label";
    private final String CONFIG_PD_DETAILS = "org.deepamehta.signup.config_pd_detail";
    
    private Topic currentModuleConfiguration = null;

    @Inject
    private AccessControlService acService;

    @Inject /*** Used in migration */
    private WorkspacesService wsService;



    @Override
    public void init() {
        initTemplateEngine();
        currentModuleConfiguration = getCurrentSignupConfiguration();
        currentModuleConfiguration.loadChildTopics();
        log.info("Sign-up: Loaded module configuration (uri=" + currentModuleConfiguration.getUri()
                + ") " + currentModuleConfiguration.getSimpleValue());
    }

    static DeepaMehtaEvent USER_ACCOUNT_CREATE_LISTENER = new DeepaMehtaEvent(UserAccountCreateListener.class) {
        @Override
        public void deliver(EventListener listener, Object... params) {
            ((UserAccountCreateListener) listener).userAccountCreated((Topic) params[0]);
        }
    };

    /** Plugin Service Implementation */
    @GET
    @Path("/sign-up/check/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUsernameAvailability(@PathParam("username") String username) {
        JSONObject response = new JSONObject();
        try {
            response.put("isAvailable", true);
            if (!isUsernameAvailable(username)) response.put("isAvailable", false);
            //
            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/sign-up/check/mailbox/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMailboxAvailability(@PathParam("email") String email) {
        JSONObject response = new JSONObject();
        try {
            response.put("isAvailable", true);
            if (isMailboxRegistered(email)) response.put("isAvailable", false);
            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Re-load the sign-up configuration topic.
     */
    @GET
    @Path("/sign-up/config/reload")
    public Topic reloadConfiguration() {
        log.info("Sign-up: Reloading sign-up plugin configuration.");
        currentModuleConfiguration = getCurrentSignupConfiguration();
        log.info("Sign-up: Loaded sign-up configuration => \"" + currentModuleConfiguration.getUri()
                + "\", \"" + currentModuleConfiguration.getSimpleValue() + "\"");
        return currentModuleConfiguration;
    }

    @GET
    @Path("/sign-up/create/{username}/{pass-one}/{mailbox}")
    @Transactional
    public String createSimpleUserAccount(@PathParam("username") String username, @PathParam("pass-one") String password,
            @PathParam("mailbox") String mailbox) {
        try {
            if (!isUsernameAvailable(username)) throw new WebApplicationException(412);
            Credentials creds = new Credentials(new JSONObject()
                    .put("username", username)
                    .put("password", password));
            log.info("Trying to set up new \"User Account\" for username " + username);
            try {
                // 1) Create new user
                Topic usernameTopic = acService.createUserAccount(creds);
                // 2) fire custom event ### useless since fired by "anonymous" (this request scope)
                // dms.fireEvent(USER_ACCOUNT_CREATE_LISTENER, user);
                // 3) attach e-mail address topic
                Topic eMailAddress = dms.createTopic(new TopicModel("dm4.contacts.email_address", new SimpleValue(mailbox.trim())));
                // 4) associate e-mail address topic to "username" topic and to "System" workspace
                AccessControl acCore = dms.getAccessControl();
                acCore.assignToWorkspace(eMailAddress, acCore.getSystemWorkspaceId());
                log.info("Assigned eMail-Address topic to system workspace");
                Association assoc = dms.createAssociation(new AssociationModel("dm4.core.association",
                        new TopicRoleModel(usernameTopic.getId(), "dm4.core.parent"),
                        new TopicRoleModel(eMailAddress.getId(), "dm4.core.child")));
                log.info("FINISHED: Related E-Mail Address: " + mailbox + " to new username");
                // 5) ### associate association to "system" workspace too
                // acCore.assignToWorkspace(assoc, acCore.getSystemWorkspaceId());
                // 6) Inform administrations
                sendNotificationMail(username, mailbox.trim());
                return username;
            } catch (Exception e) {
                log.log(Level.SEVERE, "Creating simple user account failed.", e);
                throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
            }
        } catch (JSONException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }



    /** --- Private Helpers --- */

    private void sendNotificationMail(String username, String mailbox) {
        // Fix: Classloader issue we have in OSGi since using Pax web
        Thread.currentThread().setContextClassLoader(SignupPlugin.class.getClassLoader());
        log.info("BeforeSend: Set classloader to " + Thread.currentThread().getContextClassLoader().toString());

        HtmlEmail email = new HtmlEmail();
        email.setDebug(true); // => System.out.println(SMTP communication);
        email.setHostName("localhost");
        try {
            // ..) Set Senders of Mail
            email.setFrom("mre@deepamehta.de", "My DeepaMehta 4");
        } catch (EmailException ex) {
            log.log(Level.SEVERE, null, ex);
        }

        try {
            // ..) Set Subject of Mail
            email.setSubject( "Account registration on my.deepamehta.de");
        } catch (Exception e) {
            log.log(Level.INFO, "Exception during setting subject of mail", e);
        }

        try {
            String text = "Hi there, " + username + " ("+mailbox+") created an account at your service.";
            // ..) Set Message Body
            email.setTextMsg(text);
        } catch (Exception e) {
            log.log(Level.INFO, "Exception during setting mail body", e);
        }

        // ..) Set recipient of notification mail
        String recipient = currentModuleConfiguration.getChildTopics().getString("org.deepamehta.signup.config_admin_mailbox");
        log.info("Loaded current configuration topic, sending notification mail to " + recipient);
        try {
            Collection<InternetAddress> recipients = new ArrayList<InternetAddress>();
            recipients.add(new InternetAddress(recipient));
            email.setTo(recipients);
        } catch (AddressException ex) {
            Logger.getLogger(SignupPlugin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EmailException ex) {
            Logger.getLogger(SignupPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            email.send();
            log.info("Mail was SUCCESSFULLY sent to " + email.getToAddresses() + " mail addresses");
        } catch (EmailException e) {
            log.log(Level.SEVERE, "Mail was NOT sent to " + email.getToAddresses() + " mail addresses", e);
        } catch (Exception e) { // error after send
            log.log(Level.SEVERE,"Mail was NOT sent to " + email.getToAddresses() + " mail addresses", e);
        }
        // Fix: Classloader issue we have in OSGi since using Pax web
        Thread.currentThread().setContextClassLoader(DeepaMehtaService.class.getClassLoader());
        log.info("AfterSend: Set Classloader back to " + Thread.currentThread().getContextClassLoader().toString());
    }

    private boolean isUsernameAvailable(String username) {
        Topic userName = acService.getUsernameTopic(username);
        return (userName == null);
    }

    private boolean isMailboxRegistered(String email) {
        String value = email.trim();
        Topic eMail = dms.getTopic(MAILBOX_TYPE_URI, new SimpleValue(value));
        ResultList<RelatedTopic> topics = dms.getTopics(MAILBOX_TYPE_URI, 0);
        for (RelatedTopic topic : topics) {
            if (topic.getSimpleValue().toString().contains(value)) {
                log.info("Sign-up check for E-Mail Address: " + email + " is already registered.");
                return true;
            }
        }
        log.info("Sign-up check for E-Mail Address: " + email + " seems NOT to be registered yet, or? " + (eMail != null));
        return (eMail != null);
    }

    /**
     * The sign-up configuration object is loaded once when this bundle/plugin is
     * initialized by the framework and when "admin" reloads it.
     *
     * @see init()
     */
    private Topic getCurrentSignupConfiguration() {
        Topic pluginTopic = dms.getTopic("uri", new SimpleValue("org.deepamehta.sign-up"));
        return pluginTopic.getRelatedTopic("dm4.core.association", "dm4.core.default", "dm4.core.default",
                "org.deepamehta.signup.configuration");
    }



    /** --- Sign-up Routes --- */

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable getLoginFormView() {
        // fixme: use acl service to check if a session already exists and if so, redirect to dm-webclient directly
        prepareSignupPage();
        return view("login");
    }

    @GET
    @Path("/sign-up")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSignupFormView() {
        // fixme: use acl service to check if a session already exists and if so, redirect to dm-webclient directly
        prepareSignupPage();
        return view("sign-up");
    }

    @GET
    @Path("/ok")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAccountCreationOKView() {
        prepareSignupPage();
        return view("ok");
    }

    private void prepareSignupPage() {
        if (currentModuleConfiguration != null) {
            log.info("Preparing views according to current module configuration.");
            ChildTopics configuration = currentModuleConfiguration.getChildTopics();
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
        } else {
            log.warning("Could not load module configuration!");
        }

    }

}
