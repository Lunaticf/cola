package cola.transport.netty.client;

import cola.common.RpcRequest;
import cola.common.RpcResponse;
import cola.serialization.Serializer;
import cola.transport.netty.codec.RpcDecoder;
import cola.transport.netty.codec.RpcEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author lcf
 */
public class RpcClientInitializer extends ChannelInitializer {
    private Serializer serializer;

    public RpcClientInitializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(new RpcEncoder(serializer));
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new RpcDecoder(serializer));
        cp.addLast(new RpcClientHandler());
    }
}
