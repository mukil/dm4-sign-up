package systems.dmx.signup.migrations;


import java.util.logging.Logger;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

/**
 * Extends the Sign-up Plugin Configuration about "API" related sign-up configuration options.
 */
public class Migration7 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    // ### TODO: We have (deliberately) missed to create new Workspace Assignments (for these new child topics) here.
    @Inject
    private WorkspacesService wsService;

    @Override
    public void run() {

        logger.info("### Extending Sign-up Configuration about \"API Workspace\" configuration options ###");

        TopicType signupConfigType = dmx.getTopicType("org.deepamehta.signup.configuration");
        //
        signupConfigType.addCompDef(mf.newCompDefModel(
                "org.deepamehta.signup.configuration",
                "org.deepamehta.signup.config_api_enabled", "dmx.core.one"));
        signupConfigType.addCompDef(mf.newCompDefModel(
                "org.deepamehta.signup.configuration",
                "org.deepamehta.signup.config_api_workspace_uri", "dmx.core.one"));
        signupConfigType.addCompDef(mf.newCompDefModel(
                "org.deepamehta.signup.configuration",
                "org.deepamehta.signup.config_api_description", "dmx.core.one"));
        signupConfigType.addCompDef(mf.newCompDefModel(
                "org.deepamehta.signup.configuration",
                "org.deepamehta.signup.config_api_details", "dmx.core.one"));

        // Set new default config values
        Topic defaultConfiguration = dmx.getTopicByUri("org.deepamehta.signup.default_configuration");
        dmx.updateTopic(
                mf.newTopicModel(defaultConfiguration.getId(), 
                        mf.newChildTopicsModel()
                                .set("org.deepamehta.signup.config_api_details", "No API, no Terms of service.")
                )
        );
        dmx.updateTopic(
                mf.newTopicModel(defaultConfiguration.getId(), 
                        mf.newChildTopicsModel()
                                .set("org.deepamehta.signup.config_api_enabled", false)
                )
        );
        dmx.updateTopic(
                mf.newTopicModel(defaultConfiguration.getId(), 
                        mf.newChildTopicsModel()
                                .set("org.deepamehta.signup.config_api_description", "API unavailable")
                )
        );
        dmx.updateTopic(
                mf.newTopicModel(defaultConfiguration.getId(), 
                        mf.newChildTopicsModel()
                                .set("org.deepamehta.signup.config_api_workspace_uri", "undefined")
                )
        );

    }

}