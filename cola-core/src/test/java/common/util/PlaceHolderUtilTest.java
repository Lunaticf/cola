package common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lcf
 */
public class PlaceHolderUtilTest {
    @Test
    public void test() {
        Assert.assertEquals("测试123占位符: 异常,错误",
                PlaceHolderUtil.replace("测试{}占位符: {},{}", 123, "异常", "错误"));
    }
}
