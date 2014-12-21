package org.deepamehta.plugins.signup.migrations;

import de.deepamehta.core.AssociationDefinition;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.AssociationDefinitionModel;
import de.deepamehta.core.service.Migration;
import java.util.Collection;
import java.util.logging.Logger;
import org.deepamehta.plugins.signup.SignupPlugin;

public class Migration1 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void run() {

        TopicType account = dms.getTopicType(SignupPlugin.USER_ACCOUNT_TYPE_URI);

        // If not already done, enrich the "User Account"-Type about a "E-Mail Address"-Type
        Collection<AssociationDefinition> childTypes = account.getAssocDefs();
        boolean hasMailboxAsChild = false;
        for (AssociationDefinition child : childTypes) {
            if (child.getChildTypeUri().equals(SignupPlugin.MAILBOX_TYPE_URI)) hasMailboxAsChild = true;
        }
        if (!hasMailboxAsChild) {
            logger.info("Sign-up => Enriching \"User Account\"-Type about \"E-Mail Address\"-Type");
            account.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
                    SignupPlugin.USER_ACCOUNT_TYPE_URI, SignupPlugin.MAILBOX_TYPE_URI,
                    "dm4.core.one", "dm4.core.one"));
        } else {
            logger.info("Sign-up => NOT Enriching \"User Account\"-Type about \"E-Mail Address\"-Type "
                    + "- Already done!");
        }

    }

}