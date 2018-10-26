package objective.taskboard.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BackgroundTaskTest {

    private ExecutorService executor;
    private TestBackgroundTask task;

    @Before
    public void setup() {
        executor = Executors.newSingleThreadExecutor();
        task = new TestBackgroundTask(executor);
    }

    @After
    public void stopExecutor() {
        executor.shutdown();
    }

    @Test
    public void happyLifecycle() throws Exception {
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.created);

        assertThat(task.getExecutionStart()).isNull();
        startTask();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.running);
        assertThat(task.getExecutionStart()).isNotNull();

        assertThat(task.getExecutionStop()).isNull();
        completeTask();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.finished);
        assertThat(task.getResult()).isNotNull();
        assertThat(task.getExecutionStop()).isNotNull();
    }

    @Test
    public void errorLifecycle() throws Exception {
        startTask();

        crashTask();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.error);
        assertThat(task.getResult()).isNull();
    }

    @Test
    public void cancelLifecycle() throws Exception {
        startTask();

        cancelTask();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.cancelled);
        assertThat(task.getResult()).isNull();
    }

    @Test
    public void startMultipleTimes() throws Exception {
        task.runningFuture = new CompletableFuture<>();
        task.executeFuture = new CompletableFuture<>();
        FutureTask<String> result1 = task.start();
        FutureTask<String> result2 = task.start();
        task.runningFuture.get(1, TimeUnit.SECONDS);
        FutureTask<String> result3 = task.start();
        assertThat(result1).isEqualTo(result2).isEqualTo(result3);
    }

    @Test
    public void cancelMultipleTimes() throws Exception {
        startTask();

        task.finishFuture = new CompletableFuture<>();
        task.cancel();
        task.cancel();
        task.finishFuture.get(1, TimeUnit.SECONDS);
        task.cancel();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.cancelled);
    }

    @Test
    public void startThenCancel_startThenError_startThenComplete() throws Exception {
        startTask();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.running);
        cancelTask();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.cancelled);

        startTask();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.running);
        crashTask();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.error);

        startTask();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.running);
        completeTask();
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.finished);
        assertThat(task.getResult()).isNotNull();
    }

    @Test
    public void cancelBeforeStart_noOp() throws Exception {
        task.runningFuture = new CompletableFuture<>();
        task.executeFuture = new CompletableFuture<>();
        task.finishFuture = new CompletableFuture<>();
        task.cancel();
        try {
            task.finishFuture.get(1, TimeUnit.SECONDS);
            fail("should timeout");
        } catch (TimeoutException e) {
            // ignore exception
        }
        assertThat(task.getStatus()).isEqualTo(BackgroundTask.Status.created);
        assertThat(task.runningFuture.isDone()).isFalse();
        assertThat(task.executeFuture.isDone()).isFalse();
        assertThat(task.finishFuture.isDone()).isFalse();
    }

    private void startTask() throws Exception {
        task.runningFuture = new CompletableFuture<>();
        task.executeFuture = new CompletableFuture<>();
        task.start();
        task.runningFuture.get(1, TimeUnit.SECONDS);
    }

    private void completeTask() throws Exception {
        task.finishFuture = new CompletableFuture<>();
        task.executeFuture.complete("ok");
        task.finishFuture.get(1, TimeUnit.SECONDS);
    }

    private void crashTask() throws Exception {
        task.finishFuture = new CompletableFuture<>();
        task.executeFuture.completeExceptionally(new RuntimeException("error"));
        task.finishFuture.get(1, TimeUnit.SECONDS);
    }

    private void cancelTask() throws Exception {
        task.finishFuture = new CompletableFuture<>();
        task.cancel();
        task.finishFuture.get(1, TimeUnit.SECONDS);
    }

    static class TestBackgroundTask extends BackgroundTask<String> {

        private CompletableFuture<String> executeFuture;
        private CompletableFuture<String> runningFuture;
        private CompletableFuture<String> finishFuture;

        public TestBackgroundTask(ExecutorService executor) {
            super(executor);
        }

        @Override
        protected String execute() throws Exception {
            return executeFuture.get();
        }

        @Override
        protected void runningState() {
            super.runningState();
            runningFuture.complete("called");
        }

        @Override
        protected void finish(String result, Exception error) {
            super.finish(result, error);
            finishFuture.complete("called");
        }
    }
}
