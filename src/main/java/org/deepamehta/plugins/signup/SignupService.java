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

@Path("/")
public class SignupService extends WebActivatorPlugin {

    private static Logger log = Logger.getLogger(SignupService.class.getName());

	/** see also @de.deepamehta.plugins.accesscontrol.model.Credentials */
	private static final String ENCRYPTED_PASSWORD_PREFIX = "-SHA256-";

    public final static String USERNAME_TYPE_URI = "dm4.accesscontrol.username";
    public final static String USER_ACCOUNT_TYPE_URI = "dm4.accesscontrol.user_account";
    public final static String USER_PASSWORD_TYPE_URI = "dm4.accesscontrol.password";

    public final static String MAILBOX_TYPE_URI = "dm4.contacts.email_address";

    @Override
    public void init() {
        log.info("Sign-upService Plugin INIT - Setting up the ThymeleafTemplateEngine... ");
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
            log.warning(e.getMessage());
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/sign-up/create")
    public Viewable createSimpleUserAccount(@QueryParam("username") String username,
            @QueryParam("mailbox") String mailbox, @QueryParam("pass-one") String password) {
        try {
			// fixme: owner and creator of this topic should be set to, well: _this_ (topic)
			if (!isUsernameAvailable(username, null)) throw new WebApplicationException(412);
			if (!isPasswordGood(password, null)) throw new WebApplicationException(412);
            log.info("setting up user account composite value model");
			CompositeValueModel userAccount = new CompositeValueModel()
					.put(USERNAME_TYPE_URI, username)
					.put(USER_PASSWORD_TYPE_URI, encryptPassword(password))
                    .put(MAILBOX_TYPE_URI, mailbox);
			/** CompositeValueModel personData =  new CompositeValueModel()
					.put(MAILBOX_TYPE_URI, mailbox);
			userAccount.put(PERSON_TYPE_URI, personData); **/
			// fixme: userAccount.
			// fixme: set user account to "Blocked" until verified
			TopicModel userModel = new TopicModel(USER_ACCOUNT_TYPE_URI, userAccount);
            log.info("creating topic model");
			Topic user = dms.createTopic(userModel, null);
            log.info("created topic");
			return getReceivedView(null);
        } catch (Exception e) {
            log.warning(e.getMessage());
            StackTraceElement[] traces = e.getStackTrace();
            StringBuffer cause = new StringBuffer();
            for (StackTraceElement trace : traces) {
                cause.append(trace.toString() + "\r\n");
            }
            log.warning(cause.toString());
            throw new WebApplicationException(e.getCause());
        }
    }

    @GET
    @Path("/sign-up/create/{username}/{pass-one}/{mailbox}")
    public Viewable createSimpleUserAccount(@PathParam("username") String username, @PathParam("mailbox") String mailbox,
            @PathParam("pass-one") String password, @HeaderParam("Cookie") ClientState clientState) {

        try {
			// fixme: drop involvement of clientState
			// fixme: owner and creator of this topic should be set to, well: _this_ (topic)
			if (!isUsernameAvailable(username, null)) throw new WebApplicationException(412);
			if (!isPasswordGood(password, null)) throw new WebApplicationException(412);
            log.info("setting up user account composite value model");
			CompositeValueModel userAccount = new CompositeValueModel()
					.put(USERNAME_TYPE_URI, username)
					.put(USER_PASSWORD_TYPE_URI, encryptPassword(password))
					.put(MAILBOX_TYPE_URI, mailbox);
			// fixme: userAccount.
			// fixme: set user account to "Blocked" until verified
			TopicModel userModel = new TopicModel(USER_ACCOUNT_TYPE_URI, userAccount);
            log.info("creating topic model");
			Topic user = dms.createTopic(userModel, null);
            log.info("created topic");
			return getReceivedView(null);
        } catch (Exception e) {
            log.warning(e.getMessage());
            StackTraceElement[] traces = e.getStackTrace();
            StringBuffer cause = new StringBuffer();
            for (StackTraceElement trace : traces) {
                cause.append(trace.toString() + "\r\n");
            }
            log.warning(cause.toString());
            throw new WebApplicationException(e.getCause());
        }
    }

    @POST
    @Path("/sign-up/login")
    public Viewable doLogin(@FormParam("username") String username, @FormParam("password") String password,
			@HeaderParam("Cookie") ClientState clientState) {
        try {
			// fixme: check login-credentials
			return getReceivedView(clientState);
        } catch (Exception e) {
            log.warning(e.getMessage());
            throw new WebApplicationException(e);
        }
    }

	/** --- private helpers --- */

	private boolean isUsernameAvailable(String username, ClientState clientState) {
		// fixme: framework should also allow us to query case insensitve for a username
		Topic userName = dms.getTopic(USERNAME_TYPE_URI, new SimpleValue(username), true);
		return (userName == null) ? true : false;
	}

	private boolean isPasswordGood(String password, ClientState clientState) {
		// fixem: should be at least 13 chars long but actually we should work on implementing two-factor auth
		return (password.length() >= 8) ? true : false;
	}

	private String encryptPassword(String password) {
        return ENCRYPTED_PASSWORD_PREFIX + JavaUtils.encodeSHA256(password);
    }

	/** --- routes --- */

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable getLoginView(@HeaderParam("Cookie") ClientState clientState) {
		// fixme: use acl service to check if a session already exists and if so, redirect to dm-webclient directly
        return view("login");
    }

    @GET
    @Path("/sign-up")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getSignupView(@HeaderParam("Cookie") ClientState clientState) {
		// fixme: use acl service to check if a session already exists and if so, redirect to dm-webclient directly
        return view("sign-up");
    }

    @GET
    @Path("/ok")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getReceivedView(@HeaderParam("Cookie") ClientState clientState) {
        return view("ok");
    }

}
