package systems.dmx.signup;

import systems.dmx.core.Topic;
import systems.dmx.core.service.EventListener;

/**
 *
 * @author mukil
 */
public interface UserAccountCreateListener extends EventListener {
    
    void userAccountCreated(Topic usernameTopic);

}
