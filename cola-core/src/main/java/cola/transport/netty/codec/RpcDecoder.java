package cola.transport.netty.codec;

import cola.common.RpcRequest;
import cola.serialization.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author lcf
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private static final int LENGTH_FIELD_LENGTH = 4;

    private Serializer serializer;

    public RpcDecoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 前4个字节是长度 check数据长度
        if (in.readableBytes() < LENGTH_FIELD_LENGTH) {
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
        RpcRequest rpcRequest = serializer.deserialize(data, RpcRequest.class);
        out.add(rpcRequest);
    }
}
