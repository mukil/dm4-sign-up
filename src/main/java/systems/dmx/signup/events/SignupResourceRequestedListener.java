package systems.dmx.signup.events;

import org.thymeleaf.context.AbstractContext;
import systems.dmx.core.service.EventListener;

/**
 *
 * @author malte
 */
public interface SignupResourceRequestedListener extends EventListener {

    void signupResourceRequested(AbstractContext context, String templateName);

}
