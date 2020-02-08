package cola.common;

/**
 * @author lcf
 * 回调函数
 */
public interface RpcCallback {
    void success(Object result);

    void fail(Exception e);
}
