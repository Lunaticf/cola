package serialization.api;

import common.exception.RpcException;

/**
 * @author lcf
 */
public interface Serializer {
    <T> byte[] serialize(T obj) throws RpcException;

    <T> T deserialize(byte[] data, Class<T> cls) throws RpcException;
}
