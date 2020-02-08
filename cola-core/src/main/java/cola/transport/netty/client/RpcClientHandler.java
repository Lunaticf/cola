package cola.transport.netty.client;

import cola.common.RpcFuture;
import cola.common.RpcRequest;
import cola.common.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author lcf
 * 维护一个与服务器的连接 ConnectManager为客户端维护多个这样的handler
 */
@Slf4j
@Data
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    // requestId -> rpcFuture
    private ConcurrentHashMap<String, RpcFuture> pendingRpc = new ConcurrentHashMap<>();

    private volatile Channel channel;
    private SocketAddress remotePeer;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // 保存一下服务器地址
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        // 拿到channel引用
        this.channel = ctx.channel();
    }

    /**
     * 接收Rpc响应
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        String requestId = response.getRequestId();
        RpcFuture rpcFuture = pendingRpc.get(requestId);
        if (rpcFuture != null) {
            // 该次rpc请求已经完成
            pendingRpc.remove(requestId);
            rpcFuture.done(response);
        }
    }

    /**
     * 发送Rpc请求 同步
     */
    public RpcFuture sendRequest(RpcRequest request) {
        CountDownLatch latch = new CountDownLatch(1);
        RpcFuture rpcFuture = new RpcFuture(request);

        // 存放Rpc请求对应的future
        pendingRpc.put(request.getRequestId(), rpcFuture);

        // 这个发送操作本身是异步的 所以我们用latch堵塞
        channel.writeAndFlush(request).addListener(
                (ChannelFutureListener) future -> latch.countDown()
        );

        try {
            // 发送完成
            latch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        return rpcFuture;
    }



    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client caught exception", cause);
        ctx.close();
    }
}
