package cola.common.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lcf
 * 每收到一个请求，活跃数加1，完成请求后则将活跃数减1。
 */
public class RpcStatic {
    private static final Map<String, Integer> ACTIVE_COUNT = new ConcurrentHashMap<>();

    public synchronized static int getCount(String interfaceName, String methodName, String address) {
        String key = generateKey(interfaceName, methodName, address);
        if (!ACTIVE_COUNT.containsKey(key)) {
            return 0;
        }
        return ACTIVE_COUNT.get(key);
    }

    public synchronized static void incCount(String interfaceName, String methodName, String address) {
        String key = generateKey(interfaceName, methodName, address);
        ACTIVE_COUNT.put(key, ACTIVE_COUNT.getOrDefault(key, 0) + 1);
    }

    public synchronized static void decCount(String interfaceName, String methodName, String address) {
        String key = generateKey(interfaceName, methodName, address);
        ACTIVE_COUNT.put(key, ACTIVE_COUNT.get(key) - 1);
    }

    private static String generateKey(String interfaceName, String methodName, String address) {
        return interfaceName + "." + methodName + "." + address;
    }
}
