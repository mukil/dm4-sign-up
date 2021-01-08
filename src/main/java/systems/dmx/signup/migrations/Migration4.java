package systems.dmx.signup.migrations;

import java.util.logging.Logger;
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

        // Defunct: Migrated item has now moved int platform conf ("dmx.signup.confirm_email_address")
        
    }

}