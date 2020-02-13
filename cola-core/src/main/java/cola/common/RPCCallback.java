package cola.common;

/**
 * @author lcf
 * 回调函数
 */
@FunctionalInterface
public interface RPCCallback {
    void invoke(RPCResponse response);
}
