package cola.transport.netty.client;

import cola.common.RPCRequest;
import cola.common.RPCResponse;
import cola.serialization.Serializer;
import cola.codec.codec.RPCDecoder;
import cola.codec.codec.RPCEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author lcf
 * Channel初始化器
 */
public class RPCChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Serializer serializer;

    public RPCChannelInitializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(new RPCEncoder(RPCRequest.class, serializer));
        cp.addLast(new RPCDecoder(RPCResponse.class, serializer));
        cp.addLast(new RPCResponseHandler());
    }
}
