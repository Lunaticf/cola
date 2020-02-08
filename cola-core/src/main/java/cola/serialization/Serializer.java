package cola.serialization;


import java.io.IOException;

/**
 * @author lcf
 */
public interface Serializer {
    /**
     * 序列化
     */
    <T> byte[] serialize(T obj) throws IOException;

    /**
     * 反序列化
     */
    <T> T deserialize(byte[] data, Class<T> cls) throws IOException;
}
