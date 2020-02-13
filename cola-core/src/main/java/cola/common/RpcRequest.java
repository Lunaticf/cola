package cola.common;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author lcf
 * rpc请求
 */
@Data
@Builder
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;

    /**
     * 调用哪个服务的哪个方法
     */
    private String interfaceName;
    private String methodName;

    /**
     * 调用的参数类型和参数
     */
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    public String key() {
        return interfaceName +
                "." +
                methodName +
                "." +
                Arrays.toString(parameterTypes) +
                "." +
                Arrays.toString(parameters);
    }
}
