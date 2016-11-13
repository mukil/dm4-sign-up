package org.deepamehta.plugins.signup.migrations;

import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Migration;
import de.deepamehta.workspaces.WorkspacesService;

import java.util.logging.Logger;

/**
 * Extends the Sign-up Plugin Configuration about all "API" related configuration options.
 */
public class Migration12 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    private WorkspacesService wsService;

    @Override
    public void run() {
        logger.info("###### Move Topic \"Api Membership Request Helper Note\" to \"System\"");
        // Move Topic "Api Membership Request Helper Note" to "Administration"
        Topic apiMembershipNote = dm4.getTopicByUri("org.deepamehta.signup.api_membership_requests");
        wsService.assignToWorkspace(apiMembershipNote, dm4.getAccessControl().getSystemWorkspaceId());
        logger.info("###### \"Api Membership Request Helper Note\" topic migration to \"System\" workspace complete");
    }

}