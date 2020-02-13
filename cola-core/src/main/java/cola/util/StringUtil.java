package cola.util;

import java.net.InetSocketAddress;

/**
 * @author lcf
 */
public class StringUtil {
    public static InetSocketAddress str2socket(String str) throws Exception {
        if (str == null) {
            return null;
        }

        String[] parts = str.split(":");

        if (parts.length != 2) {
            throw new Exception("socket address解析异常");
        }
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        return new InetSocketAddress(host, port);
    }
}
