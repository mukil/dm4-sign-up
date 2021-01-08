package systems.dmx.signup.migrations;

import java.util.logging.Logger;
import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.Topic;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

/**
 * Extends the Sign-up Plugin Configuration about all "API" related configuration options.
 */
public class Migration9 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    private WorkspacesService wsService;

    @Override
    public void run() {

        logger.info("### Assigning \"API Workspace Membership Request Topic\" to System Workspace ###");
        Topic systemWorkspace = wsService.getWorkspace(AccessControlService.SYSTEM_WORKSPACE_URI);
        Topic apiMembershipNote = dmx.getTopicByUri("dmx.signup.api_membership_requests");
        wsService.assignToWorkspace(apiMembershipNote, systemWorkspace.getId());

    }

}