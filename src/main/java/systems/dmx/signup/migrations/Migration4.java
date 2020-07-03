package systems.dmx.signup.migrations;

import java.util.logging.Logger;
import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

/**
 * Extends the Sign-up Plugin Configuration about a flag to enable/disable the token/mail based confirmation workflow.
 */
public class Migration4 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    private WorkspacesService wsService;

    @Override
    public void run() {

        logger.info("### Extending Sign-up Configuration about \"Email Confirmation Required\" option ###");

        Topic systemWorkspace = wsService.getWorkspace(AccessControlService.SYSTEM_WORKSPACE_URI);
        TopicType tokenConfirmationType = dmx.getTopicType("org.deepamehta.signup.config_email_confirmation");
        wsService.assignTypeToWorkspace(tokenConfirmationType, systemWorkspace.getId());

        TopicType configComposite = dmx.getTopicType("org.deepamehta.signup.configuration");
        configComposite.addCompDef(mf.newCompDefModel(
                "org.deepamehta.signup.configuration", "org.deepamehta.signup.config_email_confirmation", "dmx.core.one"));

        Topic standardConfig = dmx.getTopicByUri("org.deepamehta.signup.default_configuration");
        standardConfig.loadChildTopics();
        // ### standardConfig.getChildTopics().set("org.deepamehta.signup.config_email_confirmation", false);

        logger.info("### Setup new Sign-up Config Default: No Email confirmation required during account creation");

    }

}