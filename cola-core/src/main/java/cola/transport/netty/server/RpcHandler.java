package cola.transport.netty.server;

import cola.common.RpcRequest;
import cola.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author lcf
 */
@Slf4j
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final Map<String, Object> handlerMap;

    private RpcServer server;

    public RpcHandler(Map<String, Object> handlerMap, RpcServer rpcServer) {
        this.server = rpcServer;
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        server.handleRpcExecute(() -> {
            log.debug("收到请求 " + request.getRequestId());

            // 生成response
            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());
            try {
                Object result = handle(request);
                response.setResult(result);
            } catch (Throwable t) {
                response.setError(toString());
                log.error("Rpc request handle error! ", t);
            }

            // 写回响应
            ctx.writeAndFlush(response).addListener(
                    (ChannelFutureListener) channelFuture -> log.debug("发送响应 " + request.getRequestId())
            );
        });
    }

    /**
     * 找到rpc请求对应的服务对象 执行方法
     */
    private Object handle(RpcRequest request) throws Throwable {
        // 解析请求
        String      className = request.getClassName();
        String      methodName = request.getMethodName();
        Class<?>[]  parameterTypes = request.getParameterTypes();
        Object[]    parameters = request.getParameters();

        // 根据服务名 找到服务对象
        Object      serviceBean = handlerMap.get(className);
        Class<?>    serviceClass = serviceBean.getClass();

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
