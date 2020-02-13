package cola.registry;

import java.util.List;

/**
 * @author lcf
 */
public interface ServiceRegistry {

    List<String> discover(String service);

    void registry(String service, String address);

    void stop();
}
