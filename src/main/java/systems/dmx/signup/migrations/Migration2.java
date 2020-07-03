package systems.dmx.signup.migrations;

import com.sun.nio.sctp.Association;
import java.util.List;
import java.util.logging.Logger;
import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.Assoc;
import systems.dmx.core.Topic;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

public class Migration2 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    private WorkspacesService wsService;

    @Override
    public void run() {

        Topic pluginTopic = dmx.getTopicByUri("org.deepamehta.sign-up");
        Topic standardConfiguration = dmx.getTopicByUri("org.deepamehta.signup.default_configuration");
        // 1) Assign the (default) "Sign-up Configuration" to the Plugin topic
        List<Assoc> configs = pluginTopic.getAssocs();
        boolean hasConfiguration = false;
        for (Assoc assoc : configs) {
            if (assoc.getPlayer1().getDMXObject().getTypeUri().equals("org.deepamehta.signup.configuration")) hasConfiguration = true;
        }
        if (!hasConfiguration) {
            logger.info("Sign-up => Assigning default \"Sign-up Configuration\" to \"DeepaMehta 4 Sign up\" Topic");
            Assoc assoc = dmx.createAssoc(mf.newAssocModel("dmx.core.association",
                    mf.newTopicPlayerModel(pluginTopic.getId(), "dmx.core.default"),
                    mf.newTopicPlayerModel(standardConfiguration.getId(), "dmx.core.default")
            ));
            Topic systemWorkspace = wsService.getWorkspace(AccessControlService.SYSTEM_WORKSPACE_URI);
            wsService.assignToWorkspace(assoc, systemWorkspace.getId());
        } else {
            logger.info("Sign-up => NOT assigning \"Sign-up Configuration\" to \"DeepaMehta 4 Sign up\" Topic"
                    + "- Already done!");
        }
        // 2) Set Configuration Topic Workspace Assignment to ("System") (editable for admin)
        Topic systemWorkspace = wsService.getWorkspace(AccessControlService.SYSTEM_WORKSPACE_URI);
        wsService.assignToWorkspace(standardConfiguration, systemWorkspace.getId());

    }

}