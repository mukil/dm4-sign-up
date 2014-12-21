package org.deepamehta.plugins.signup.migrations;

import de.deepamehta.core.Association;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.service.Migration;
import java.util.List;
import java.util.logging.Logger;

public class Migration3 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void run() {

        Topic pluginTopic = dms.getTopic("uri", new SimpleValue("org.deepamehta.sign-up"));

        // If not already done, enrich the "User Account"-Type about a "E-Mail Address"-Type
        List<Association> configs = pluginTopic.getAssociations();
        boolean hasConfiguration = false;
        for (Association assoc : configs) {
            if (assoc.getPlayer1().getTypeUri().equals("org.deepamehta.signup.configuration")) hasConfiguration = true;
        }
        if (!hasConfiguration) {
            logger.info("Sign-up => Assigning default \"Sign-up Configuration\" to \"DeepaMehta 4 Sign up\" Topic");
            dms.createAssociation(new AssociationModel("dm4.core.association", 
                    new TopicRoleModel(pluginTopic.getId(), "dm4.core.default"),
                    new TopicRoleModel("org.deepamehta.signup.wikidata_topicmaps_configuration", "dm4.core.default")
            ));
        } else {
            logger.info("Sign-up => NOT assigning \"Sign-up Configuration\" to \"DeepaMehta 4 Sign up\" Topic"
                    + "- Already done!");
        }

    }

}