package objective.taskboard.testUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

public class OperationLoggers {

    private OperationLoggers() {
    }

    public static OperationLogger create() {
        return new BaseLogger();
    }
    
    public static OperationLogger wrap(OperationLogger logger, String defaultMarker) {
        return new WrappedLogger(logger, defaultMarker);
    }
    
    public interface OperationLogger {
        void append(String operation, String... markers);
        String getLog(Collection<String> markersToFilter);
        String getAllLog();
        void clear();
    }
    
    private static class BaseLogger implements OperationLogger {
        private final List<OperationLog> logs = new ArrayList<>();
        
        @Override
        public void append(String operation, String... markers) {
            logs.add(new OperationLog(operation, markers));
        }

        @Override
        public String getLog(Collection<String> markersToFilter) {
            return logs.stream()
                    .filter(log -> markersToFilter == null ? true : log.containsAnyMarker(markersToFilter))
                    .map(OperationLog::getOperation)
                    .map(String::trim)
                    .collect(Collectors.joining("\n"));
        }

        @Override
        public String getAllLog() {
            return getLog(null);
        }

        @Override
        public void clear() {
            logs.clear();
        }
    }
    
    private static class OperationLog {
        private final String operation;
        private final Set<String> markers;

        public OperationLog(String operation, String...  markers) {
            this.operation = operation;
            this.markers = new HashSet<>(Arrays.asList(markers));
        }
        
        public String getOperation() {
            return operation;
        }
        
        public boolean containsAnyMarker(Collection<String> markers) {
            Set<String> markersToTest = new HashSet<>(markers);
            return this.markers.stream().anyMatch(m -> markersToTest.contains(m));
        }
    }
    
    private static class WrappedLogger implements OperationLogger {
        private final OperationLogger delegate;
        private final String defaultMarker;

        public WrappedLogger(OperationLogger logger, String defaultMarker) {
            this.delegate = logger;
            this.defaultMarker = defaultMarker;
        }

        @Override
        public void append(String operation, String... markers) {
            this.delegate.append(operation, ArrayUtils.add(markers, defaultMarker));
        }

        @Override
        public String getLog(Collection<String> markersToFilter) {
            return delegate.getLog(markersToFilter);
        }

        @Override
        public String getAllLog() {
            return delegate.getAllLog();
        }

        @Override
        public void clear() {
            delegate.clear();
        }
    }
}
