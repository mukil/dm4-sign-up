package org.deepamehta.plugins.signup;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.*;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.core.service.annotation.ConsumesService;
import de.deepamehta.plugins.accesscontrol.model.ACLEntry;
import de.deepamehta.plugins.accesscontrol.model.AccessControlList;
import de.deepamehta.plugins.accesscontrol.model.Operation;
import de.deepamehta.plugins.accesscontrol.model.UserRole;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
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
 * @name org.deepamehta-sign-up
 * @website https://github.com/mukil/org.deepamehta-sign-up
 * @version 1.0.0-SNAPSHOT
 * @author malt
 */

@Path("/")
public class SignupService extends WebActivatorPlugin {

    private static Logger log = Logger.getLogger(SignupService.class.getName());

	/** @see also @de.deepamehta.plugins.accesscontrol.model.Credentials */
	private static final String ENCRYPTED_PASSWORD_PREFIX = "-SHA256-";

    public final static String USERNAME_TYPE_URI = "dm4.accesscontrol.username";
    public final static String USER_ACCOUNT_TYPE_URI = "dm4.accesscontrol.user_account";
    public final static String USER_PASSWORD_TYPE_URI = "dm4.accesscontrol.password";

    public final static String MAILBOX_TYPE_URI = "dm4.contacts.email_address";

    public final static String WS_DM_DEFAULT_URI = "de.workspaces.deepamehta";
    public final static String WS_WIKIDATA_URI = "org.deepamehta.workspaces.wikidata";

    private AccessControlService acService = null;

    @Override
    public void init() {
        initTemplateEngine();
	}

    /** Plugin Service Implementation */

    @GET
    @Path("/sign-up/check/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUsernameAvailability(@PathParam("username") String username,
			@HeaderParam("Cookie") ClientState clientState) {
		JSONObject response = new JSONObject();
        try {
			response.put("isAvailable", true);
			if (!isUsernameAvailable(username, clientState)) response.put("isAvailable", false);
			//
			return response.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/sign-up/create/{username}/{pass-one}/{mailbox}")
    public String createSimpleUserAccount(@PathParam("username") String username, @PathParam("mailbox") String mailbox,
            @PathParam("pass-one") String password) {

        try {
			if (!isUsernameAvailable(username, null)) throw new WebApplicationException(412);
			if (!isPasswordGood(password, null)) throw new WebApplicationException(412);
            log.info("Setting up new \"User Account\" composite value model");
			CompositeValueModel userAccount = new CompositeValueModel()
					.put(USERNAME_TYPE_URI, username)
					.put(USER_PASSWORD_TYPE_URI, password)
					.put(MAILBOX_TYPE_URI, mailbox);
			// ### set user account to "Blocked" until verified (introduce this in a new migration)
			TopicModel userModel = new TopicModel(USER_ACCOUNT_TYPE_URI, userAccount);
			Topic user = dms.createTopic(userModel, null);
            log.info("Created new \"User Account\" for " + username);
            acService.setACL(user, new AccessControlList( //
                            new ACLEntry(Operation.WRITE, UserRole.OWNER)));
            acService.setCreator(user, username);
            acService.setOwner(user, username);
            log.warning("ACL-Properties are now set for " + username);
            assignToDefaultWorkspace(user.getCompositeValue().getTopic(USERNAME_TYPE_URI));
			return username;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    /** --- Implementing PluginService Interfaces to consume AccessControlService --- */

    @Override
    @ConsumesService(de.deepamehta.plugins.accesscontrol.service.AccessControlService.class)
    public void serviceArrived(PluginService service) {
        if (service instanceof AccessControlService) {
            acService = (AccessControlService) service;
        }
    }

    @Override
    @ConsumesService(de.deepamehta.plugins.accesscontrol.service.AccessControlService.class)
    public void serviceGone(PluginService service) {
        if (service == acService) {
            acService = null;
        }
    }



    /** --- Private Helpers --- */

	private boolean isUsernameAvailable(String username, ClientState clientState) {
		// fixme: framework should also allow us to query case insensitve for a username
		Topic userName = dms.getTopic(USERNAME_TYPE_URI, new SimpleValue(username), true);
		return (userName == null) ? true : false;
	}

	private boolean isPasswordGood(String password, ClientState clientState) {
		// fixem: should be at least 13 chars long but actually we should work on implementing two-factor auth
		return (password.length() >= 8) ? true : false;
	}

    private void assignToDefaultWorkspace(Topic topic) {
        Topic defaultWorkspace = dms.getTopic("uri", new SimpleValue(WS_DM_DEFAULT_URI), false);
        dms.createAssociation(new AssociationModel("dm4.core.aggregation",
            new TopicRoleModel(topic.getId(), "dm4.core.parent"),
            new TopicRoleModel(defaultWorkspace.getId(), "dm4.core.child")
        ), null);
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

}
