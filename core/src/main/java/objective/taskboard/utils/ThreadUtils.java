package objective.taskboard.utils;

public class ThreadUtils {

    public static void sleepOrCry(long timeToWait) {
        sleepOrCry(timeToWait, null);
    }

    public static void sleepOrCry(long timeToWait, String message) {
        try {
            Thread.sleep(timeToWait);
        } catch (InterruptedException e) {//NOSONAR
            if (message == null)
                throw new IllegalStateException(e);
            throw new IllegalStateException(message, e);
        }
    }

}
