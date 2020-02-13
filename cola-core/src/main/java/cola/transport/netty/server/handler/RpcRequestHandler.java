package cola.transport.netty.server.handler;

import cola.common.RpcRequest;
import cola.common.RpcResponse;
import cola.transport.netty.server.RpcServer;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author lcf
 * RPC请求handler
 */
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private Map<String, Object> handlerMap;

    private RpcServer server;

    public RpcRequestHandler(Map<String, Object> handlerMap, RpcServer rpcServer) {
        this.handlerMap = handlerMap;
        this.server = rpcServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        // 丢到线程池执行
        server.handleRpcExecute(() -> {
            log.debug("Get request:{} ", request.getRequestId());

            // 生成response
            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());
            try {
                Object result = handle(request);
                response.setResult(result);
            } catch (Exception e) {
                response.setError(e);
                log.error("Rpc request handle error! {}", e.getMessage());
            }

            // 写回响应
            ctx.writeAndFlush(response).addListener(
                    (ChannelFutureListener) channelFuture -> log.debug("发送响应 {}",request.getRequestId())
            );
        });
    }

    /**
     * 找到rpc请求对应的服务对象 执行方法
     */
    private Object handle(RpcRequest request) throws Exception {
        // 解析请求
        String      serviceName = request.getInterfaceName();
        String      methodName = request.getMethodName();
        Class<?>[]  parameterTypes = request.getParameterTypes();
        Object[]    parameters = request.getParameters();

        // 根据服务名 找到服务对象
        Object      serviceBean = handlerMap.get(serviceName);
        Class<?>    serviceClass = serviceBean.getClass();

        if (serviceBean == null) {
            throw new RuntimeException(String.format("No service bean available: %s", serviceName));
        }

        log.debug(serviceClass.getName());
        log.debug(methodName);

        // 调用反射执行服务对象的方法
        // JDK reflect 已经不慢于cglib

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    /**
     * 重写捕获错误方法 方便我们打印错误
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server caught exception", cause);
        ctx.close();
    }
}
