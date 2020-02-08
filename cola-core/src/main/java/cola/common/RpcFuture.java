package cola.common;

import cola.transport.netty.client.RpcClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lcf
 * 异步调用返回结果 Future只是一个接口
 */
@Slf4j
public class RpcFuture implements Future<Object> {

    private Sync sync;
    private RpcRequest request;
    private RpcResponse response;

    private long startTime;

    /**
     * 请求阈值时间
     */
    private long responseTimeThreshold = 5000;

    /**
     *  存放所有callback
     */
    private List<RpcCallback> pendingCallbacks = new ArrayList<RpcCallback>();


    public RpcFuture(RpcRequest request) {
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);

        // 如果已经有响应了
        if (this.response != null) {
            return this.response.getResult();
        } else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // 尝试自旋时间
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
    }

    /**
     *  存放响应结果
     */
    public void done(RpcResponse response) {
        this.response = response;
        sync.release(1);

        // 可以调用callback
        invokeCallbacks();
        // Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            log.warn("Service response time is too slow. Request id = " + response.getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    /**
     * 调用callback
     */
    private void invokeCallbacks() {
        for (final RpcCallback callback : pendingCallbacks) {
            runCallback(callback);
        }
    }

    private void runCallback(final RpcCallback callback) {
        final RpcResponse res = this.response;
        RpcClient.submit(() -> {

            // 根据错误执行不同回调
            if (!res.isError()) {
                callback.success(res.getResult());
            } else {
                callback.fail(new RuntimeException("Response error", new Throwable(res.getError())));
            }
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


    static class Sync extends AbstractQueuedSynchronizer {

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == 1;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == 0) {
                if (compareAndSetState(0, 1)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        public boolean isDone() {
            return getState() == 1;
        }
    }
}
