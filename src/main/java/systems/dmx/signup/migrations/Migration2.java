package systems.dmx.signup.migrations;

import java.util.logging.Logger;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

public class Migration2 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    private WorkspacesService wsService;

    @Override
    public void run() {

        /** Topic plugin = dmx.getTopicByUri(SignupPlugin.SIGNUP_SYMOBILIC_NAME);
        Topic standardConfiguration = dmx.getTopicByUri("dmx.signup.default_configuration");
        // 1) Assign the (default) "Sign-up Configuration" to the Plugin topic
        logger.info("Sign-up => Assigning default \"Sign-up Configuration\" to \"DMX Sign up\" Topic");
        Assoc assoc = dmx.createAssoc(mf.newAssocModel("dmx.core.association",
                mf.newTopicPlayerModel(plugin.getId(), "dmx.core.default"),
                mf.newTopicPlayerModel(standardConfiguration.getId(), "dmx.core.default")
        ));
        // 2) Set Configuration Topic Workspace Assignment to ("System") (editable for admin)
        Topic systemWorkspace = wsService.getWorkspace(AccessControlService.SYSTEM_WORKSPACE_URI);
        wsService.assignToWorkspace(standardConfiguration, systemWorkspace.getId());
        wsService.assignToWorkspace(assoc, systemWorkspace.getId()); **/

    }

}