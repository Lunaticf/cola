package serialization.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import common.exception.RpcException;
import org.objenesis.strategy.StdInstantiatorStrategy;
import serialization.api.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author lcf
 *
 */
public class KyroSeralizer implements Serializer {
    /**
     * Kyro是线程不安全的 每个线程使用自己的Kryo
     * ThreadLocal可以将一个非线程安全的对象转换成支持多线程访问的对象
     * 最常见的使用是包裹线程不安全的工具类对象
     */
    private static final ThreadLocal<Kryo> THREAD_LOCAL = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            return kryo;
        }
    };


    public <T> byte[] serialize(T obj) throws RpcException {
        Output output = new Output(new ByteArrayOutputStream());
        Kryo kryo = THREAD_LOCAL.get();
        kryo.writeObject(output, obj);
        return output.toBytes();
    }

    public <T> T deserialize(byte[] data, Class<T> cls) throws RpcException {
        Input input = new Input(new ByteArrayInputStream(data));
        Kryo kryo = THREAD_LOCAL.get();
        return kryo.readObject(input, cls);
    }
}
