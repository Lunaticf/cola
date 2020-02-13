package cola.common;

/**
 * @author lcf
 * 回调函数
 */
@FunctionalInterface
public interface RpcCallback {
    void invoke(RpcResponse response);
}
