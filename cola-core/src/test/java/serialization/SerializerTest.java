package serialization;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import serialization.hessian.HessianSerializer;
import serialization.jdk.JdkSerializer;
import serialization.json.JsonSerializer;
import serialization.kyro.KyroSeralizer;

import java.io.Serializable;

/**
 * @author lcf
 * 测试不同的序列化方式
 */
public class SerializerTest {
    @Data
    static public class SomeClass implements Serializable {
        String value;
    }

    @Test
    public void JDKTest() {
        SomeClass object = new SomeClass();
        object.value = "Hello world!";

        JdkSerializer js = new JdkSerializer();
        byte[] bytes = js.serialize(object);

        SomeClass someClass = js.deserialize(bytes, SomeClass.class);
        Assert.assertEquals("Hello world!", someClass.value);
    }

    @Test
    public void JsonTest() {
        SomeClass object = new SomeClass();
        object.value = "Hello world!";

        JsonSerializer js = new JsonSerializer();

        byte[] bytes = js.serialize(object);

        SomeClass someClass = js.deserialize(bytes, SomeClass.class);
        Assert.assertEquals("Hello world!", someClass.value);
    }

    @Test
    public void HessianTest() {
        SomeClass object = new SomeClass();
        object.value = "Hello world!";

        HessianSerializer hs = new HessianSerializer();
        byte[] bytes = hs.serialize(object);

        SomeClass someClass = hs.deserialize(bytes, SomeClass.class);
        Assert.assertEquals("Hello world!", someClass.value);
    }

    @Test
    public void KyroTest() {
        SomeClass object = new SomeClass();
        object.value = "Hello world!";

        KyroSeralizer ks = new KyroSeralizer();
        byte[] bytes = ks.serialize(object);

        SomeClass someClass = ks.deserialize(bytes, SomeClass.class);
        Assert.assertEquals("Hello world!", someClass.value);
    }
}
