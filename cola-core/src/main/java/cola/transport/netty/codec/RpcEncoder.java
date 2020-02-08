package cola.transport.netty.codec;

import cola.serialization.Serializer;
import cola.transport.netty.constant.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author lcf
 */
public class RpcEncoder extends MessageToByteEncoder {
    private Serializer serializer;

    public RpcEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        out.writeInt(Constant.LENGTH_FIELD_LENGTH);
        out.writeBytes(serializer.serialize(msg));
    }
}
