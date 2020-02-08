package cola.serialization.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import cola.serialization.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author lcf
 */
public class HessianSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream baos;
        Hessian2Output hop = null;

        try {
            baos = new ByteArrayOutputStream();
            hop = new Hessian2Output(baos);

            hop.writeObject(obj);
            hop.flush();
            return baos.toByteArray();
        } catch (Throwable e) {
            throw new IOException("序列化异常");
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

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) throws IOException {
        Hessian2Input hip = null;
        try {
            hip = new Hessian2Input(new ByteArrayInputStream(data));
            return cls.cast(hip.readObject(cls));
        } catch (Throwable e) {
            throw new IOException("反序列化异常");
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
