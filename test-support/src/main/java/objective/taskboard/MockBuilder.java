package objective.taskboard;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class MockBuilder<T> {

    protected final Class<T> mockClass;
    protected final Map<Function<T, Object>, Object> config = new LinkedHashMap<>();

    public MockBuilder(Class<T> mockClass) {
        this.mockClass = mockClass;
    }

    public T build() {
        T mock = mock(mockClass);
        config.forEach((methodRef, value) -> when(methodRef.apply(mock)).thenReturn(value));
        return mock;
    }
}