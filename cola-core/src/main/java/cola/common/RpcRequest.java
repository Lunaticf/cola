package cola.common;

import lombok.Data;

/**
 * @author lcf
 * rpc请求
 */
@Data
public class RpcRequest {
    private String requestId;

    /**
     * 调用哪个服务的哪个方法
     */
    private String className;
    private String methodName;

    /**
     * 调用的参数类型和参数
     */
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}
