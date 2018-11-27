package objective.taskboard.utils;

import java.util.function.Function;

public abstract class ObjectUtils {

    /**
     * Utility method that performs all commons checks involving an object comparison, 
     * like identity equality, null reference and type cast.
     * 
     * @param thisObject
     * @param otherObject
     * @param otherIsEquals Accepts the "other" object and should return {@code true} if it is equal to "this" object.
     * @return true if objects are equals
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean equals(T thisObject, Object otherObject, Function<T, Boolean> otherIsEquals) {
        if (thisObject == otherObject)
            return true;

        if (otherObject == null || !thisObject.getClass().isInstance(otherObject))
            return false;
        
        return otherIsEquals.apply((T) otherObject);
    }
}
