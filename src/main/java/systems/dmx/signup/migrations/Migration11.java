package systems.dmx.signup.migrations;

import java.util.logging.Logger;
import systems.dmx.core.RelatedTopic;
import systems.dmx.core.Topic;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

/**
 * Assign four left over Sign-up Plugin Configuration topic childs to "Administration" workspace.
 */
public class Migration11 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    private WorkspacesService wsService;

    @Override
    public void run() {
        logger.info("###### Assign four more Sign-up Configration child topics to \"Administration\" Workspace");
        long administrationWsId = dmx.getPrivilegedAccess().getAdminWorkspaceId();
        Topic standardConfiguration = dmx.getTopicByUri("org.deepamehta.signup.default_configuration");
        wsService.assignToWorkspace(standardConfiguration, administrationWsId);
        RelatedTopic loadingAppHint = standardConfiguration.getChildTopics().getTopic("org.deepamehta.signup.loading_app_hint");
        RelatedTopic loggingOutHint = standardConfiguration.getChildTopics().getTopic("org.deepamehta.signup.logging_out_hint");
        RelatedTopic startPageUrl= standardConfiguration.getChildTopics().getTopic("org.deepamehta.signup.start_page_url");
        RelatedTopic homePageUrl= standardConfiguration.getChildTopics().getTopic("org.deepamehta.signup.home_page_url");
        wsService.assignToWorkspace(loadingAppHint, administrationWsId);
        wsService.assignToWorkspace(loadingAppHint.getRelatingAssoc(), administrationWsId);
        wsService.assignToWorkspace(loggingOutHint, administrationWsId);
        wsService.assignToWorkspace(loggingOutHint.getRelatingAssoc(), administrationWsId);
        wsService.assignToWorkspace(startPageUrl, administrationWsId);
        wsService.assignToWorkspace(startPageUrl.getRelatingAssoc(), administrationWsId);
        wsService.assignToWorkspace(homePageUrl, administrationWsId);
        wsService.assignToWorkspace(homePageUrl.getRelatingAssoc(), administrationWsId);
    }

}