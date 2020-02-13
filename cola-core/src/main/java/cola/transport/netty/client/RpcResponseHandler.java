package cola.transport.netty.client;

import cola.common.RpcFuture;
import cola.common.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lcf
 */
@Slf4j
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        System.out.println("收到远方的响应");
        String requestId = response.getRequestId();
        RpcFutureManager.getInstance().futureDone(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("RPC request exception: {}", cause);
    }

}
