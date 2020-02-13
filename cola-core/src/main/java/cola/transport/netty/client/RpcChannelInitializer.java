package cola.transport.netty.client;

import cola.common.RpcRequest;
import cola.common.RpcResponse;
import cola.serialization.Serializer;
import cola.transport.netty.codec.RpcDecoder;
import cola.transport.netty.codec.RpcEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.HashMap;

/**
 * @author lcf
 * Channel初始化器
 */
public class RpcChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Serializer serializer;

    public RpcChannelInitializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(new RpcEncoder(RpcRequest.class, serializer));
        cp.addLast(new RpcDecoder(RpcResponse.class, serializer));
        cp.addLast(new RpcResponseHandler());

    }
}
