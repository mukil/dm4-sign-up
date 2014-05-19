package org.deepamehta.plugins.signup.migrations;

import de.deepamehta.core.AssociationDefinition;
import java.util.logging.Logger;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Migration3 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    private String USER_ACCOUNT_TYPE_URI = "dm4.accesscontrol.user_account";
    private String DISPLAY_NAME_TYPE_URI = "org.deepamehta.identity.display_name";
    private String CONTACT_TYPE_URI = "org.deepamehta.identity.contact";
    private String INFO_TYPE_URI = "org.deepamehta.identity.infos";

    @Override
    public void run() {

        TopicType account = dms.getTopicType(USER_ACCOUNT_TYPE_URI, null);
        logger.info("Sign-up Migration3 => Enriching \"User Account\"-Type about \"Contact\", \"Info\" and "
                + "\"Display Name\"-Type");
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def", USER_ACCOUNT_TYPE_URI,
            DISPLAY_NAME_TYPE_URI, "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def", USER_ACCOUNT_TYPE_URI,
            CONTACT_TYPE_URI, "dm4.core.one", "dm4.core.one"));
        account.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def", USER_ACCOUNT_TYPE_URI,
            INFO_TYPE_URI, "dm4.core.one", "dm4.core.one"));

    }

}