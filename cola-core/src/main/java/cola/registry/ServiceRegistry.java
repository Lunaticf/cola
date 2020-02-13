package cola.registry;

import java.util.List;

/**
 * @author lcf
 */
public interface ServiceRegistry {

    static String REGISTRY_PATH = "/cola";
    static String PROVIDER_PATH = "/provider";
    static String CONSUMER_PATH = "/consumer";

    List<String> discover(String service);

    void registry(String service, String address);

    /**
     *  根据服务名和地址组装znode path
     */
    default String generatePath(String service, String address) {
        return REGISTRY_PATH + "/" + service + ServiceRegistry.PROVIDER_PATH + "/" + address;
    }

    /**
     * com.cola.HelloService -> /cola/com.cola.HelloService/provider
     */
    default String genServicePath(String service) {
        return REGISTRY_PATH + "/" + service + PROVIDER_PATH;
    }
}
