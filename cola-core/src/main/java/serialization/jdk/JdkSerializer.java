package serialization.jdk;

import common.enumeration.ErrorType;
import common.exception.RpcException;
import common.util.IoUtil;
import serialization.api.Serializer;

import java.io.*;

/**
 * @author lcf
 * @date 2020-01-17 23:33
 */
public class JdkSerializer implements Serializer {
    public <T> byte[] serialize(T obj) throws RpcException {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);

            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (Throwable e) {
            throw new RpcException(e, ErrorType.SERIALIZER_ERROR, "序列化异常: {}", obj);
        } finally {
            IoUtil.closeQuietly(oos);
        }
    }

    public <T> T deserialize(byte[] data, Class<T> cls) throws RpcException {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        try {
            bais = new ByteArrayInputStream(data);
            ois = new ObjectInputStream(bais);

            return cls.cast(ois.readObject());
        } catch (Throwable e) {
            throw new RpcException(e, ErrorType.SERIALIZER_ERROR, "反序列化异常: {}", cls);
        } finally {
            IoUtil.closeQuietly(ois);
        }
    }
}
