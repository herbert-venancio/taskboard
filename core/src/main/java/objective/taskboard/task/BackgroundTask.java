package objective.taskboard.task;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BackgroundTask<T> {

    protected static final Logger LOG = LoggerFactory.getLogger(BackgroundTask.class);

    private final ExecutorService executor;

    private float progress;
    private Status status;
    private T result;
    private Instant executionStart;
    private Instant executionStop;

    private final Lock read;
    private final Lock write;
    private transient FutureTask<T> futureTask;

    public BackgroundTask(ExecutorService executor) {
        this.executor = executor;
        this.progress = 0.0f;
        this.status = Status.created;
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        read = readWriteLock.readLock();
        write = readWriteLock.writeLock();
    }

    public FutureTask<T> start() {
        return withWriteLock(() -> {
            if(futureTask == null) {
                futureTask = new FutureTask<>(this::executeTask);
                executor.execute(futureTask);
            }
            return futureTask;
        });
    }

    public void cancel() {
        withWriteLock(() -> {
            if(futureTask != null) {
                futureTask.cancel(true);
            }
        });
    }

    private T executeTask() {
        runningState();
        T result = null;
        Exception error = null;
        try {
            result = execute();
        } catch (Exception t) {
            error = t;
        } finally {
            finish(result, error);
        }
        return result;
    }

    protected abstract T execute() throws Exception;

    /**
     * Represents how much of the background task is done
     * @return A value between 0.0f and 1.0f (percentage done)
     */
    public float getProgress() {
        return withReadLock(() -> progress);
    }

    protected void setProgress(float value) {
        withWriteLock(() -> {
            progress = value;
        });
    }

    public Status getStatus() {
        return withReadLock(() -> status);
    }

    public Instant getExecutionStart() {
        return withReadLock(() -> executionStart);
    }

    public Instant getExecutionStop() {
        return withReadLock(() -> executionStop);
    }

    public T getResult() {
        return withReadLock(() -> result);
    }

    /**
     * Sets this task to running state
     */
    protected void runningState() {
        withWriteLock(() -> {
            executionStart = Instant.now();
            progress = 0;
            status = Status.running;
        });
    }

    /**
     * Sets this task to finished, cancelled or error state, depending on the outcome of the task execution
     * @param result the result returned by the task execution, or null if an exception was thrown
     * @param error the exception thrown by the task execution, or null if the execution finished successfully
     */
    protected void finish(T result, Exception error) {
        withWriteLock(() -> {
            if(error == null) {
                progress = 1.0f;
                status = Status.finished;
            } else if (futureTask.isCancelled()) {
                status = Status.cancelled;
            } else {
                status = Status.error;
                LOG.error("An exception occurred while running BackgroundTask", error);
            }

            executionStop = Instant.now();
            this.result = result;
            futureTask = null;
        });
    }

    /**
     * Checks if current thread has received interrupt signal to cancel execution
     * @throws InterruptedException if task was cancelled
     */
    protected void checkInterruption() throws InterruptedException {
        if(Thread.interrupted())
            throw new InterruptedException();
    }

    protected <U> U withWriteLock(Supplier<U> supplier) {
        return withLock(write, supplier);
    }

    protected void withWriteLock(Runnable runnable) {
        withLock(write, runnable);
    }

    protected <U> U withReadLock(Supplier<U> supplier) {
        return withLock(read, supplier);
    }

    private <U> U withLock(Lock lock, Supplier<U> supplier) {
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    private void withLock(Lock lock, Runnable runnable) {
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    public enum Status {
        created,
        running,
        finished,
        error,
        cancelled
    }
}
