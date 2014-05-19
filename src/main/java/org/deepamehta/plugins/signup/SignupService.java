package org.deepamehta.plugins.signup;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.CompositeValueModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.util.JavaUtils;
import de.deepamehta.plugins.webactivator.WebActivatorPlugin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;
import javax.ws.rs.*;
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

    @Override
    public void init() {
        initTemplateEngine();
	}

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


    /**
     * TODO: Maybe switch to QueryParams, like:
     *  public Viewable createSimpleUserAccount(@QueryParam("username") String username,
     *      @QueryParam("mailbox") String mailbox, @QueryParam("pass-one") String password)
     */
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
            // fixme: owner and creator of this topic should be set to, well: _this_ (topic)
			TopicModel userModel = new TopicModel(USER_ACCOUNT_TYPE_URI, userAccount);
			Topic user = dms.createTopic(userModel, null);
            log.info("Created new \"User Account\" for " + username);
            log.warning("ACL-Properties should be set for " + username);
			return username;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
