package cola.executor;

public interface TaskExecutor {
    void init(int threads);
    void submit(Runnable runnable);
    void close();
}
