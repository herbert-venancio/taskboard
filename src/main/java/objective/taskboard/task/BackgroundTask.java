package objective.taskboard.task;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class BackgroundTask<T> {

    private final ExecutorService executor;

    private float progress;
    private Status status;
    private T result;
    private Instant executionStart;
    private Instant executionStop;

    protected final ReentrantReadWriteLock.ReadLock read;
    protected final ReentrantReadWriteLock.WriteLock write;
    private transient FutureTask<T> futureTask;

    public BackgroundTask(ExecutorService executor) {
        this.executor = executor;
        this.progress = 0.0f;
        this.status = Status.created;
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        read = readWriteLock.readLock();
        write = readWriteLock.writeLock();
    }

    public FutureTask<T> start() {
        write.lock();
        try {
            if(futureTask == null) {
                futureTask = new FutureTask<>(this::executeTask);
                executor.execute(futureTask);
            }
            return futureTask;
        } finally {
            write.unlock();
        }
    }

    public void cancel() {
        write.lock();
        try {
            if(futureTask != null) {
                futureTask.cancel(true);
            }
        } finally {
            write.unlock();
        }
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

    public float getProgress() {
        read.lock();
        try {
            return progress;
        } finally {
            read.unlock();
        }
    }

    protected void setProgress(float value) {
        write.lock();
        try {
            progress = value;
        } finally {
            write.unlock();
        }
    }

    public Status getStatus() {
        read.lock();
        try {
            return status;
        } finally {
            read.unlock();
        }
    }

    public Instant getExecutionStart() {
        read.lock();
        try {
            return executionStart;
        } finally {
            read.unlock();
        }
    }

    public Instant getExecutionStop() {
        read.lock();
        try {
            return executionStop;
        } finally {
            read.unlock();
        }
    }

    public T getResult() {
        read.lock();
        try {
            return result;
        } finally {
            read.unlock();
        }
    }

    protected void runningState() {
        write.lock();
        try {
            executionStart = Instant.now();
            progress = 0;
            status = Status.running;
        } finally {
            write.unlock();
        }
    }

    protected void finish(T result, Exception error) {
        write.lock();
        try {
            if(error == null) {
                progress = 1.0f;
                status = Status.finished;
            } else if (futureTask.isCancelled()) {
                status = Status.cancelled;
            } else {
                status = Status.error;
                error.printStackTrace();
            }
            executionStop = Instant.now();
            this.result = result;
            futureTask = null;
        } finally {
            write.unlock();
        }
    }

    protected void checkInterruption() throws InterruptedException {
        Thread.sleep(0);
    }

    public enum Status {
        created,
        running,
        finished,
        error,
        cancelled
    }
}
