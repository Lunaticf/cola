package cola.codec.codec;

import cola.serialization.Serializer;
import cola.transport.netty.constant.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * @author lcf
 */
public class RPCDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;
    private Serializer serializer;

    public RPCDecoder(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // 前4个字节是长度 check数据长度
        if (in.readableBytes() < Constant.LENGTH_FIELD_LENGTH) {
            return;
        }

        // 请求应有的长度
        in.markReaderIndex();
        int len = in.readInt();

        // 如果数据不够头部的长度
        if (in.readableBytes() < len) {
            in.resetReaderIndex();
            return;
        }

        // 可以处理 包正常
        byte[] data = new byte[len];
        in.readBytes(data);

        // 把二进制反序列化
        Object obj = serializer.deserialize(data, genericClass);
        ReferenceCountUtil.retain(obj);
        out.add(obj);
    }
}
