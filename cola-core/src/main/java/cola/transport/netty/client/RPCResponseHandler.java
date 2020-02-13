package cola.transport.netty.client;

import cola.common.RPCResponse;
import cola.filter.FilterManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author lcf
 */
@Slf4j
public class RPCResponseHandler extends SimpleChannelInboundHandler<RPCResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponse response) throws Exception {
        // after filter
        if (FilterManager.afterFilter != null) {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            FilterManager.afterFilter.invoke(response, socketAddress.getAddress() + ":" + socketAddress.getPort());
        }

        RPCFutureManager rpcFutureManager = RPCFutureManager.getInstance();
        rpcFutureManager.futureDone(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("RPC request exception: {}", cause);
    }

}
