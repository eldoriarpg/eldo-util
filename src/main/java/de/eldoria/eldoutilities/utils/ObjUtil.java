package de.eldoria.eldoutilities.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * General handling of objects.
 *
 * @since 1.0.0
 */
public final class ObjUtil {
    private ObjUtil() {
    }

    /**
     * Consumes a object if object is not null
     *
     * @param obj     object to condume
     * @param execute consumer to handle object
     * @param <T>     type of object
     * @return true if the object was not null and the consumer was applied
     */
    public static <T> boolean nonNull(@Nullable T obj, Consumer<T> execute) {
        if (obj == null) return false;
        execute.accept(obj);
        return true;
    }

    /**
     * Consumes a object if object is not null or executes a runnable when object is null
     *
     * @param obj      object to condume
     * @param execute  consumer to handle object
     * @param runnable runnable to run when object is null
     * @param <T>      type of object
     * @return true if the object was not null and the consumer was applied false if the object was null
     */
    public static <T> boolean nonNull(@Nullable T obj, Consumer<T> execute, Runnable runnable) {
        if (obj == null) {
            runnable.run();
            return false;
        }
        execute.accept(obj);
        return true;
    }

    /**
     * Execute a function on a object if the object is not null
     *
     * @param obj     object to check
     * @param execute function to parse object to return type
     * @param <T>     object type
     * @param <U>     return type
     * @return object of return type U
     */
    public static <T, U> U nonNull(@Nullable T obj, Function<T, U> execute) {
        if (obj == null) return null;
        return execute.apply(obj);
    }

    /**
     * get the object or the other object if object is null
     *
     * @param obj         object to check
     * @param otherObject other object
     * @param <T>         type of object
     * @return object or other object if object is null
     */
    public static <T> T nonNull(@Nullable T obj, @NotNull T otherObject) {
        return obj == null ? otherObject : obj;
    }

    /**
     * Executes a function on object and return value or default value if object is null
     *
     * @param obj        object
     * @param function   function to parse
     * @param defaultVal default value
     * @param <R>        type of output object
     * @param <A>        type of input object
     * @return value of function or default value
     */
    public static <R, A> R nonNullOrElse(@Nullable A obj, Function<A, R> function, R defaultVal) {
        if (obj != null) {
            return function.apply(obj);
        }
        return defaultVal;
    }
}
