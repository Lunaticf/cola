package cola.common;

import cola.transport.netty.client.RpcClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author lcf
 * 异步调用返回结果 Future只是一个接口
 * 可能会有多个线程操作 要用锁
 */
@Slf4j
@Data
public class RpcFuture implements Future<Object> {

    private RpcRequest request;
    private RpcResponse response;

    CountDownLatch latch = new CountDownLatch(1);

    /**
     *  存放所有callback
     */
    private List<RpcCallback> pendingCallbacks = new ArrayList<RpcCallback>();


    public RpcFuture(RpcRequest request) {
        this.request = request;
    }


    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        return response.getResult();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            if (!latch.await(timeout, unit)) {
                throw new TimeoutException("RPC Request timeout!");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        return response.getResult();
    }

    /**
     *  存放响应结果
     */
    public void done(RpcResponse response) {
        this.response = response;
        latch.countDown();

        // 可以调用callback
        invokeCallbacks();
    }

    /**
     * 调用callback
     */
    private void invokeCallbacks() {
        for (final RpcCallback callback : pendingCallbacks) {
            runCallback(callback);
        }
    }

    /**
     * 执行回调函数
     */
    private void runCallback(final RpcCallback callback) {
        final RpcResponse res = this.response;
        RpcClient.submit(() -> {

            // 执行回调
            callback.invoke(res);
        });
    }

    /**
     * 添加 callback
     */
    public RpcFuture addCallback(RpcCallback callback) {
        // 如果添加的时候已经有响应了 直接运行
        if (isDone()) {
            runCallback(callback);
        } else {
            this.pendingCallbacks.add(callback);
        }
        return this;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }
}
