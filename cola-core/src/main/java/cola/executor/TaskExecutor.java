package cola.executor;

/**
 * @author lunaticf
 */
public interface TaskExecutor {

    void submit(Runnable runnable);

    void close();
}
