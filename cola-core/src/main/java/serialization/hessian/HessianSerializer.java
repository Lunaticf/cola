package serialization.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import common.enumeration.ErrorType;
import common.exception.RpcException;
import serialization.api.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author lcf
 */
public class HessianSerializer implements Serializer {
    public <T> byte[] serialize(T obj) throws RpcException {
        ByteArrayOutputStream baos = null;
        Hessian2Output hop = null;

        try {
            baos = new ByteArrayOutputStream();
            hop = new Hessian2Output(baos);

            hop.writeObject(obj);
            hop.flush();
            return baos.toByteArray();
        } catch (Throwable e) {
            throw new RpcException(e, ErrorType.SERIALIZER_ERROR, "序列化异常: {}", obj);
        } finally {
            // 关闭流
            if (hop != null) {
                try {
                    hop.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public <T> T deserialize(byte[] data, Class<T> cls) throws RpcException {
        Hessian2Input hip = null;
        try {
            hip = new Hessian2Input(new ByteArrayInputStream(data));
            return cls.cast(hip.readObject(cls));
        } catch (Throwable e) {
            throw new RpcException(e, ErrorType.SERIALIZER_ERROR, "序列化异常: {}", cls);
        } finally {
            if (hip != null) {
                try {
                    hip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
