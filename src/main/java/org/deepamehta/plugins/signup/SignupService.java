package org.deepamehta.plugins.signup;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Association;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Transactional;
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
public class SignupService extends WebActivatorPlugin {

    private static Logger log = Logger.getLogger(SignupService.class.getName());

    /** @see also @de.deepamehta.plugins.accesscontrol.model.Credentials */
    private static final String ENCRYPTED_PASSWORD_PREFIX = "-SHA256-";

    public static final String USER_ACCOUNT_TYPE_URI = "dm4.accesscontrol.user_account";
    public static final String MAILBOX_TYPE_URI = "dm4.contacts.email_address";

    private static final String USERNAME_TYPE_URI = "dm4.accesscontrol.username";
    private static final String USER_PASSWORD_TYPE_URI = "dm4.accesscontrol.password";
    private static final String WS_WIKIDATA_URI = "org.deepamehta.workspaces.wikidata";
    private static final String WS_DEFAULT_URI = "de.workspaces.deepamehta";

    public final static String WS_DM_DEFAULT_URI = "de.workspaces.deepamehta";
    
    final String ADMINISTRATOR_USERNAME = "admin";

    @Inject
    private AccessControlService acService;

    @Override
    public void init() {
        initTemplateEngine();
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
            // ### assign to custom workspace - make configurable
            assignToWikidataWorkspace(user.getChildTopics().getTopic(USERNAME_TYPE_URI)); // Membership "Wikidata"
            return username;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    

    /** --- Private Helpers --- */

    private boolean isUsernameAvailable(String username) {
        // fixme: framework should also allow us to query case insensitve for a username
        Topic userName = dms.getTopic(USERNAME_TYPE_URI, new SimpleValue(username));
        return (userName == null);
    }

    private boolean isPasswordGood(String password) {
        // fixem: should be at least 13 chars long but actually we should work on implementing two-factor auth
        return (password.length() >= 8);
    }
    

    /** --- Sign-up Routes --- */

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable getLoginFormView() {
        // fixme: use acl service to check if a session already exists and if so, redirect to dm-webclient directly
        return view("login");
    }

    @GET
    @Path("/sign-up")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSignupFormView() {
        // fixme: use acl service to check if a session already exists and if so, redirect to dm-webclient directly
        return view("sign-up");
    }

    @GET
    @Path("/ok")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAccountCreationOKView() {
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

    private void assignToWikidataWorkspace(Topic topic) {
        Topic wikidataWorkspace = dms.getTopic("uri", new SimpleValue(WS_WIKIDATA_URI));
        if (!associationExists("dm4.core.aggregation", wikidataWorkspace, topic)) {
            dms.createAssociation(new AssociationModel("dm4.core.aggregation",
                new TopicRoleModel(topic.getId(), "dm4.core.parent"),
                new TopicRoleModel(wikidataWorkspace.getId(), "dm4.core.child")
            ));
        }
    }

}
