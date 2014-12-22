package org.deepamehta.plugins.signup;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Association;
import de.deepamehta.core.ChildTopics;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Transactional;
import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
import de.deepamehta.plugins.accesscontrol.model.ACLEntry;
import de.deepamehta.plugins.accesscontrol.model.AccessControlList;
import de.deepamehta.plugins.accesscontrol.model.Operation;
import de.deepamehta.plugins.accesscontrol.model.UserRole;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
 * @version 1.0.0-SNAPSHOT
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
    private static final String USER_PASSWORD_TYPE_URI = "dm4.accesscontrol.password";
    // private static final String WS_WIKIDATA_URI = "org.deepamehta.workspaces.wikidata";
    // private static final String WS_DEFAULT_URI = "de.workspaces.deepamehta";

    public final static String WS_DM_DEFAULT_URI = "de.workspaces.deepamehta";
    private final String ADMINISTRATOR_USERNAME = "admin";
    
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

    @Override
    public void init() {
        initTemplateEngine();
        currentModuleConfiguration = getCurrentSignupConfiguration();
        currentModuleConfiguration.loadChildTopics();
        log.info("Sign-up: Set module configuration to (uri=" + currentModuleConfiguration.getUri() 
                + ") " + currentModuleConfiguration.getSimpleValue());
    }
    
    @Override
    public void postInstall() {
        checkACLsOfMigration();
    }

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
    @Path("/sign-up/create/{username}/{pass-one}/{mailbox}")
    @Transactional
    public String createSimpleUserAccount(@PathParam("username") String username, @PathParam("mailbox") String mailbox,
            @PathParam("pass-one") String password) {
        try {
            if (!isUsernameAvailable(username)) throw new WebApplicationException(412);
            if (!isPasswordGood(password)) throw new WebApplicationException(412);
            log.fine("Setting up new \"User Account\" composite value model");
            ChildTopicsModel userAccount = new ChildTopicsModel()
                .put(USERNAME_TYPE_URI, username)
                .put(USER_PASSWORD_TYPE_URI, password)
                .put(MAILBOX_TYPE_URI, mailbox);
            // ### set user account to "Blocked" until verified (introduce this in a new migration)
            TopicModel userModel = new TopicModel(USER_ACCOUNT_TYPE_URI, userAccount);
            Topic user = dms.createTopic(userModel);
            log.info("Created new \"User Account\" for " + username);
            acService.setACL(user, new AccessControlList( //
                            new ACLEntry(Operation.WRITE, UserRole.OWNER)));
            acService.setCreator(user, username);
            acService.setOwner(user, username);
            // ### assign to custom workspace - make configurable, e.g. Membership "Wikidata"
            assignToConfiguredWorkspace(user.getChildTopics().getTopic(USERNAME_TYPE_URI)); 
            return username;
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
        return currentModuleConfiguration;
    }


    

    /** --- Private Helpers --- */

    private boolean isUsernameAvailable(String username) {
        // fixme: framework should also allow us to query case insensitve for a username
        Topic userName = dms.getTopic(USERNAME_TYPE_URI, new SimpleValue(username));
        return (userName == null);
    }
    
    private Topic getCurrentSignupConfiguration() {
        Topic pluginTopic = dms.getTopic("uri", new SimpleValue("org.deepamehta.sign-up"));
        return pluginTopic.getRelatedTopic("dm4.core.association", "dm4.core.default", "dm4.core.default", 
                "org.deepamehta.signup.configuration");
    }
    
    private Topic getCurrentSignupWorkspace() {
        Topic pluginConfiguration = getCurrentSignupConfiguration();
        return pluginConfiguration.getRelatedTopic("dm4.core.association", "dm4.core.default", "dm4.core.default", 
                "dm4.workspaces.workspace");
    }

    private boolean isPasswordGood(String password) {
        // fixem: should be at least 13 chars long but actually we should work on implementing two-factor auth
        return (password.length() >= 8);
    }
    
    private void checkACLsOfMigration() {
        Topic config = dms.getTopic("uri", new SimpleValue("org.deepamehta.signup.wikidata_topicmaps_configuration"));
        if (acService.getCreator(config) == null) {
            DeepaMehtaTransaction tx = dms.beginTx();
            log.info("Sign-up: initial ACL update of configuration");
            try {
                Topic admin = acService.getUsername(ADMINISTRATOR_USERNAME);
                String adminName = admin.getSimpleValue().toString();
                acService.setCreator(config, adminName);
                acService.setOwner(config, adminName);
                acService.setACL(config, new AccessControlList(new ACLEntry(Operation.WRITE, UserRole.OWNER)));
                tx.success();
            } catch (Exception e) {
                log.warning("Sign-up: could not update ACLs of migration due to a " 
                    +  e.getClass().toString());
            } finally {
                tx.finish();
            }
            
        }
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


    private boolean associationExists(String edge_type, Topic item, Topic user) {
        List<Association> results = dms.getAssociations(item.getId(), user.getId(), edge_type);
        return (results.size() > 0);
    }

    private void assignToDefaultWorkspace(Topic topic) {
        Topic defaultWorkspace = dms.getTopic("uri", new SimpleValue(WS_DM_DEFAULT_URI));
        if (!associationExists("dm4.core.aggregation", defaultWorkspace, topic)) {
            dms.createAssociation(new AssociationModel("dm4.core.aggregation",
                new TopicRoleModel(topic.getId(), "dm4.core.parent"),
                new TopicRoleModel(defaultWorkspace.getId(), "dm4.core.child")
            ));
        } else {
            log.warning("New User Account was already to default (\"DeepaMehta\") workspace "
                + "(probably through already having a workspace cookie set?).");
        }
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

    private void assignToConfiguredWorkspace(Topic topic) {
        Topic configuredWorkspace = getCurrentSignupWorkspace();
        if (configuredWorkspace != null) {
            log.info("Sign-up: No workspace associated with \"Sign-up configuration\" topic. Assigning new "
                    + "usernames to default workspace \"DeepaMehta\".");
            if (!associationExists("dm4.core.aggregation", configuredWorkspace, topic)) {
                dms.createAssociation(new AssociationModel("dm4.core.aggregation",
                    new TopicRoleModel(topic.getId(), "dm4.core.parent"),
                    new TopicRoleModel(configuredWorkspace.getId(), "dm4.core.child")
                ));
            }
        } else {
            assignToDefaultWorkspace(topic);
        }
    }

}
