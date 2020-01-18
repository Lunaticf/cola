package serialization.json;

import com.alibaba.fastjson.JSONObject;
import common.exception.RpcException;
import serialization.api.Serializer;

/**
 * @author lcf
 */
public class JsonSerializer implements Serializer {
    public <T> byte[] serialize(T obj) throws RpcException {
        return JSONObject.toJSONBytes(obj);
    }

    public <T> T deserialize(byte[] data, Class<T> cls) throws RpcException {
        return JSONObject.parseObject(data, cls);
    }
}
