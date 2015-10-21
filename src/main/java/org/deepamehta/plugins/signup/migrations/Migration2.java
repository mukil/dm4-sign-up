package org.deepamehta.plugins.signup.migrations;

import de.deepamehta.core.Association;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Migration;
import de.deepamehta.plugins.accesscontrol.AccessControlService;
import de.deepamehta.plugins.workspaces.WorkspacesService;
import java.util.List;
import java.util.logging.Logger;

public class Migration2 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    private WorkspacesService wsService;

    @Override
    public void run() {

        Topic pluginTopic = dms.getTopic("uri", new SimpleValue("org.deepamehta.sign-up"));

        // 1) Assign the (default) "Sign-up Configuration" to the Plugin topic
        List<Association> configs = pluginTopic.getAssociations();
        boolean hasConfiguration = false;
        for (Association assoc : configs) {
            if (assoc.getPlayer1().getTypeUri().equals("org.deepamehta.signup.configuration")) hasConfiguration = true;
        }
        // ..
        if (!hasConfiguration) {
            logger.info("Sign-up => Assigning default \"Sign-up Configuration\" to \"DeepaMehta 4 Sign up\" Topic");
            Association assoc = dms.createAssociation(new AssociationModel("dm4.core.association",
                    new TopicRoleModel(pluginTopic.getId(), "dm4.core.default"),
                    new TopicRoleModel("org.deepamehta.signup.default_configuration", "dm4.core.default")
            ));
            Topic systemWorkspace = wsService.getWorkspace(AccessControlService.SYSTEM_WORKSPACE_URI);
            wsService.assignToWorkspace(assoc, systemWorkspace.getId());
        } else {
            logger.info("Sign-up => NOT assigning \"Sign-up Configuration\" to \"DeepaMehta 4 Sign up\" Topic"
                    + "- Already done!");
        }

        // 2) Set Configuration Topic Workspace Assignment to ("System") (editable for admin)
        Topic config_topic = dms.getTopic("uri",
                new SimpleValue("org.deepamehta.signup.default_configuration"));
        Topic systemWorkspace = wsService.getWorkspace(AccessControlService.SYSTEM_WORKSPACE_URI);
        wsService.assignToWorkspace(config_topic, systemWorkspace.getId());

    }

}