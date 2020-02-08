package cola.serialization.json;

import com.alibaba.fastjson.JSONObject;
import cola.serialization.Serializer;

import java.io.IOException;

/**
 * @author lcf
 */
public class JsonSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return JSONObject.toJSONBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) throws IOException {
        return JSONObject.parseObject(data, cls);
    }
}
