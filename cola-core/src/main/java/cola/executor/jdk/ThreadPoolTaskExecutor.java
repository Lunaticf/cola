package cola.executor.jdk;

import cola.executor.TaskExecutor;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lcf
 */
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolTaskExecutor implements TaskExecutor {
    private ExecutorService executorService;

    private int threads;

    public ThreadPoolTaskExecutor(int threads) {
        executorService = new ThreadPoolExecutor(
                threads,
                threads,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new ThreadFactory() {
                    private AtomicInteger atomicInteger = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r,"Thread pool - " + atomicInteger.getAndIncrement());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Override
    public void submit(Runnable runnable) {
        executorService.submit(runnable);
    }

    @Override
    public void close() {
        executorService.shutdown();
    }
}
