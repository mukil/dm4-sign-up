package systems.dmx.signup.migrations;

import java.util.logging.Logger;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.service.Migration;

/**
 * Extends the Sign-up Plugin Configuration about a "Start Page URL" and a "Home Page URL" for customizing
 * our login resp. the registration dialog (in the case of a needed confirmation).
 */
public class Migration5 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void run() {

        // ### TODO: We have (deliberately) missed to create new Workspace Assignments here.

        logger.info("### Extending Sign-up Configuration about \"Start Page URL\" option ###");
        dmx.createTopicType(mf.newTopicTypeModel("org.deepamehta.signup.start_page_url",
            "Sign-up: Start Page URL", "dmx.core.text"));

        logger.info("### Extending Sign-up Configuration about \"Home Page URL\" option ###");
        dmx.createTopicType(mf.newTopicTypeModel("org.deepamehta.signup.home_page_url",
            "Sign-up: Home Page URL", "dmx.core.text"));

        logger.info("### Extending Sign-up Configuration about \"Loading App Hint\" option ###");
        dmx.createTopicType(mf.newTopicTypeModel("org.deepamehta.signup.loading_app_hint",
            "Sign-up: Loading App Hint", "dmx.core.text"));

        logger.info("### Extending Sign-up Configuration about \"Logging Out Hint\" option ###");
        dmx.createTopicType(mf.newTopicTypeModel("org.deepamehta.signup.logging_out_hint",
            "Sign-up: Logging Out Hint", "dmx.core.text"));

        TopicType signupConfigType = dmx.getTopicType("org.deepamehta.signup.configuration");
        signupConfigType.addCompDef(mf.newCompDefModel(
                "org.deepamehta.signup.configuration", "org.deepamehta.signup.start_page_url", "dmx.core.one"));
        signupConfigType.addCompDef(mf.newCompDefModel(
                "org.deepamehta.signup.configuration", "org.deepamehta.signup.home_page_url", "dmx.core.one"));
        signupConfigType.addCompDef(mf.newCompDefModel(
                "org.deepamehta.signup.configuration", "org.deepamehta.signup.loading_app_hint", "dmx.core.one"));
        signupConfigType.addCompDef(mf.newCompDefModel(
                "org.deepamehta.signup.configuration", "org.deepamehta.signup.logging_out_hint", "dmx.core.one"));
        // Set new default config values
        Topic defaultConfiguration = dmx.getTopicByUri("org.deepamehta.signup.default_configuration");
        dmx.updateTopic(
                mf.newTopicModel(defaultConfiguration.getId(), 
                        mf.newChildTopicsModel()
                                .set("org.deepamehta.signup.start_page_url", "/systems.dmx.webclient/")
                )
        );
        dmx.updateTopic(
                mf.newTopicModel(defaultConfiguration.getId(), 
                        mf.newChildTopicsModel()
                                .set("org.deepamehta.signup.home_page_url", "/systems.dmx.webclient/")
                )
        );
        dmx.updateTopic(
                mf.newTopicModel(defaultConfiguration.getId(), 
                        mf.newChildTopicsModel()
                                .set("org.deepamehta.signup.loading_app_hint", "Loading DMX Webclient")
                )
        );
        dmx.updateTopic(
                mf.newTopicModel(defaultConfiguration.getId(), 
                        mf.newChildTopicsModel()
                                .set("org.deepamehta.signup.logging_out_hint", "Logging out..")
                )
        );
    }

}