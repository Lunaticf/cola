package cola.registry;

import java.util.List;

/**
 * @author lcf
 */
public interface ServiceRegistry {
    void init();
    List<String> discover(String service);
    void registry(String service, String address);
    void stop();
}
