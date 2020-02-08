package cola.serialization.jdk;

import cola.serialization.Serializer;

import java.io.*;

/**
 * @author lcf
 * @date 2020-01-17 23:33
 */
public class JdkSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)
        ) {
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (Throwable e) {
            throw new IOException("序列化异常");
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) throws IOException {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais)
        ) {
            return cls.cast(ois.readObject());
        } catch (Throwable e) {
            throw new IOException("反序列化异常");
        }
    }
}
