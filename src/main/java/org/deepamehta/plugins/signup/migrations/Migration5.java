package org.deepamehta.plugins.signup.migrations;

import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.AssociationDefinitionModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicTypeModel;
import de.deepamehta.core.service.Migration;

import java.util.logging.Logger;

/**
 * Extends the Sign-up Plugin Configuration about a "Start Page URL" and a "Home Page URL" for customizing
 * our login resp. the registration dialog (in the case of a needed confirmation).
 */
public class Migration5 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    // @Inject
    // private WorkspacesService wsService;

    @Override
    public void run() {

        logger.info("### Extending Sign-up Configuration about \"Start Page URL\" option ###");
        dms.createTopicType(new TopicTypeModel("org.deepamehta.signup.start_page_url",
            "Sign-up: Start Page URL", "dm4.core.text"));

        logger.info("### Extending Sign-up Configuration about \"Home Page URL\" option ###");
        dms.createTopicType(new TopicTypeModel("org.deepamehta.signup.home_page_url",
            "Sign-up: Home Page URL", "dm4.core.text"));

        logger.info("### Extending Sign-up Configuration about \"Loading App Hint\" option ###");
        dms.createTopicType(new TopicTypeModel("org.deepamehta.signup.loading_app_hint",
            "Sign-up: Loading App Hint", "dm4.core.text"));

        logger.info("### Extending Sign-up Configuration about \"Logging Out Hint\" option ###");
        dms.createTopicType(new TopicTypeModel("org.deepamehta.signup.logging_out_hint",
            "Sign-up: Logging Out Hint", "dm4.core.text"));

        TopicType signupConfigType = dms.getTopicType("org.deepamehta.signup.configuration");
        signupConfigType.addAssocDef(new AssociationDefinitionModel(
                "dm4.core.composition_def", "org.deepamehta.signup.configuration",
                "org.deepamehta.signup.start_page_url", "dm4.core.one", "dm4.core.one"));
        signupConfigType.addAssocDef(new AssociationDefinitionModel(
                "dm4.core.composition_def", "org.deepamehta.signup.configuration",
                "org.deepamehta.signup.home_page_url", "dm4.core.one", "dm4.core.one"));
        signupConfigType.addAssocDef(new AssociationDefinitionModel(
                "dm4.core.composition_def", "org.deepamehta.signup.configuration",
                "org.deepamehta.signup.loading_app_hint", "dm4.core.one", "dm4.core.one"));
        signupConfigType.addAssocDef(new AssociationDefinitionModel(
                "dm4.core.composition_def", "org.deepamehta.signup.configuration",
                "org.deepamehta.signup.logging_out_hint", "dm4.core.one", "dm4.core.one"));

        Topic defaultConfiguration = dms.getTopic("uri", new SimpleValue("org.deepamehta.signup.default_configuration"));
        defaultConfiguration.loadChildTopics();
        defaultConfiguration.getChildTopics().set("org.deepamehta.signup.start_page_url", "/de.deepamehta.webclient/");
        defaultConfiguration.getChildTopics().set("org.deepamehta.signup.home_page_url", "/de.deepamehta.webclient/");
        defaultConfiguration.getChildTopics().set("org.deepamehta.signup.loading_app_hint", "Loading Webclient..");
        defaultConfiguration.getChildTopics().set("org.deepamehta.signup.logging_out_hint", "Logging out..");

    }

}